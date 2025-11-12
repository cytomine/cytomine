package com.cytomine.registry.client.file;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cytomine.registry.client.constant.Constants;
import com.cytomine.registry.client.image.Blob;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class FileUtils {

    public static List<Blob> extractTar(InputStream is, Path dst) throws IOException {
        List<Blob> blobs = new ArrayList<>();
        TarArchiveInputStream tarArchiveIs = new TarArchiveInputStream(is) {
            @Override
            public void close() throws IOException {
                // Do not close the underlying InputStream because it managed upstream.
            }
        };
        try {
            TarArchiveEntry entry = null;
            while ((entry = tarArchiveIs.getNextTarEntry()) != null) {
                if (entry.isDirectory()) continue;
                if (!tarArchiveIs.canReadEntryData(entry))
                    throw new IOException("read tar entry error");
                Path itemPath = dst.resolve(FileUtils.replacePathChar(entry.getName()));
                Files.createDirectories(itemPath.getParent());
                Sha256HashOutputStream sha256HashOutputStream;
                try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(itemPath))) {
                    sha256HashOutputStream = new Sha256HashOutputStream(os);
                    org.apache.commons.compress.utils.IOUtils.copyRange(tarArchiveIs,
                        entry.getSize(), sha256HashOutputStream);
                }
                blobs.add(new Blob(entry.getName(), entry.getSize(),
                    Constants.SHA256_PREFIX + sha256HashOutputStream.hash(),
                    () -> Files.newInputStream(itemPath)));
            }
        } finally {
            tarArchiveIs.close();
        }
        return blobs;
    }

    public static Blob gzCompress(InputStream is) throws IOException {
        Path temp = Files.createTempFile(UUID.randomUUID().toString(), ".tar.gz");
        byte[] buffer = new byte[4096];
        Path compressed;
        Sha256HashOutputStream sha256HashOutputStream;
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(temp))) {
            sha256HashOutputStream = new Sha256HashOutputStream(os);
            GzipCompressorOutputStream gzOS =
                new GzipCompressorOutputStream(sha256HashOutputStream);
            org.apache.commons.compress.utils.IOUtils.copy(is, gzOS);
            gzOS.finish();
        }
        String sha256 = sha256HashOutputStream.hash();
        return new Blob(temp.toFile().getName(), Files.size(temp), Constants.SHA256_PREFIX + sha256,
            () -> Files.newInputStream(temp));
    }

    public static String replacePathChar(String str) {
        return str.replaceAll("[:*?\"<>|]", "/");
    }
}
