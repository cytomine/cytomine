package io.github.ya_b.registry.client.manager;

import com.google.gson.reflect.TypeToken;
import io.github.ya_b.registry.client.constant.Constants;
import io.github.ya_b.registry.client.constant.FileConstant;
import io.github.ya_b.registry.client.exception.FormatNotSupportException;
import io.github.ya_b.registry.client.exception.TarFileErrException;
import io.github.ya_b.registry.client.file.FileUtils;
import io.github.ya_b.registry.client.image.Blob;
import io.github.ya_b.registry.client.image.Context;
import io.github.ya_b.registry.client.image.Format;
import io.github.ya_b.registry.client.image.registry.ManifestHttp;
import io.github.ya_b.registry.client.image.tar.IndexFile;
import io.github.ya_b.registry.client.image.tar.ManifestFile;
import io.github.ya_b.registry.client.name.Reference;
import io.github.ya_b.registry.client.utils.IOUtils;
import io.github.ya_b.registry.client.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class FileManager {

    public Context load(InputStream is) throws IOException {
        Path dst = Files.createTempDirectory(UUID.randomUUID().toString());
        List<Blob> extractFiles = FileUtils.extractTar(is, dst);
        Format format = imageType(extractFiles);
        if (Format.DOCKER.equals(format)) {
            extractFiles = gzTarItem(extractFiles);
            return readManifest(extractFiles, dst);
        } else if (Format.OCI.equals(format)) {
            return readIndex(extractFiles, dst);
        }
        throw new FormatNotSupportException("format not support");
    }

    public void save(Context context, OutputStream os) throws IOException {
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
            TarArchiveEntry manifestEntry = new TarArchiveEntry(FileConstant.MANIFEST);
            byte[] manifestContent = JsonUtil.toJson(Collections.singletonList(context.manifestFile())).getBytes(StandardCharsets.UTF_8);
            manifestEntry.setSize(manifestContent.length);
            tos.putArchiveEntry(manifestEntry);
            tos.write(manifestContent);
            tos.closeArchiveEntry();
            List<Blob> blobs = new ArrayList<>(context.getLayers());
            blobs.add(context.getConfig());
            for (Blob blob : blobs) {
                TarArchiveEntry layerEntry = new TarArchiveEntry(blob.getName());
                layerEntry.setSize(blob.getSize());
                tos.putArchiveEntry(layerEntry);
                try (InputStream is = blob.getContent().get()) {
                    org.apache.commons.compress.utils.IOUtils.copy(is, tos);
                }
                tos.closeArchiveEntry();
            }
        }
    }

    private List<Blob> gzTarItem(List<Blob> layers) throws IOException {
        List<Blob> result = new ArrayList<>();
        for (Blob blob : layers) {
            if (blob.getName().endsWith(FileConstant.EXTENSION_TAR)) {
                Blob compressedBlob = FileUtils.gzCompress(blob.getContent().get());
                compressedBlob.setName(blob.getName());
                result.add(compressedBlob);
            } else {
                result.add(blob);
            }
        }
        return result;
    }


    private Context readIndex(List<Blob> files, Path dir) throws IOException {
        Function<String, Blob> findBlob = name -> files.stream()
                .filter(p -> Objects.equals(Paths.get(p.getName()), Paths.get(name))).findFirst().orElse(null);
        Blob index = findBlob.apply(FileConstant.INDEX);
        assert index != null;
        String indexContent = IOUtils.readString(index.getContent().get(), StandardCharsets.UTF_8);
        IndexFile indexFile = JsonUtil.fromJson(indexContent, IndexFile.class);
        assert indexFile.getManifests().size() > 0;
        Path manifestPath = Paths.get(Constants.PATH_BLOBS, FileUtils.replacePathChar(indexFile.getManifests().get(0).getDigest()));
        String manifestContent = IOUtils.readString(dir.resolve(manifestPath), StandardCharsets.UTF_8);
        ManifestHttp manifestHttp = JsonUtil.fromJson(manifestContent, ManifestHttp.class);
        Blob config = findBlob.apply(Paths.get(Constants.PATH_BLOBS, FileUtils.replacePathChar(manifestHttp.getConfig().getDigest())).toString());
        List<Blob> layers = manifestHttp.getLayers().stream()
                .map(layer -> Paths.get(Constants.PATH_BLOBS, FileUtils.replacePathChar(layer.getDigest())).toString())
                .map(findBlob)
                .collect(Collectors.toList());
        if (config == null || layers.stream().anyMatch(Objects::isNull)) {
            throw new TarFileErrException("file missing");
        }
        config.setName(config.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_TAR_GZ);
        layers.forEach(layer ->
                layer.setName(layer.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_TAR_GZ));
        return new Context(Reference.parse(indexFile.getManifests().get(0).getAnnotations().getImageRefName()), config, layers);
    }

    private Context readManifest(List<Blob> files, Path dir) throws IOException {
        Function<String, Blob> findBlob = name -> files.stream()
                .filter(p -> Objects.equals(Paths.get(p.getName()), Paths.get(name))).findFirst().orElse(null);
        Blob manifest = findBlob.apply(FileConstant.MANIFEST);
        assert manifest != null;
        String manifestContent = IOUtils.readString(manifest.getContent().get(), StandardCharsets.UTF_8);
        List<ManifestFile> manifestFiles = JsonUtil.fromJson(manifestContent, new TypeToken<List<ManifestFile>>() {});
        if (manifestFiles.size() == 0) throw new TarFileErrException("manifest.json error");
        ManifestFile manifestFile = manifestFiles.get(0);
        Blob config = findBlob.apply(manifestFile.getConfig());
        List<Blob> layers = manifestFiles.get(0).getLayers().stream().map(findBlob).collect(Collectors.toList());
        if (config == null || layers.stream().anyMatch(Objects::isNull)) {
            throw new TarFileErrException("file missing");
        }
        config.setName(config.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_TAR_GZ);
        layers.forEach(layer ->
                layer.setName(layer.getDigest().replace(Constants.SHA256_PREFIX, "") + FileConstant.EXTENSION_TAR_GZ));
        return new Context(Reference.parse(manifestFiles.get(0).getRepoTags().get(0)), config, layers);
    }

    private Format imageType(List<Blob> extractFiles) throws FormatNotSupportException {
        for (Blob extractFile : extractFiles) {
            String filename = new File(extractFile.getName()).getName();
            if (FileConstant.MANIFEST.equals(filename)) {
                return Format.DOCKER;
            } else if (FileConstant.INDEX.equals(filename)) {
                return Format.OCI;
            }
        }
        throw new FormatNotSupportException("manifest not found in tar");
    }
}
