package io.github.ya_b.registry.client.image;

import io.github.ya_b.registry.client.constant.Constants;
import io.github.ya_b.registry.client.image.registry.ManifestHttp;
import io.github.ya_b.registry.client.image.tar.ManifestFile;
import io.github.ya_b.registry.client.name.Reference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class Context {

    private Reference reference;

    private Blob config;

    private List<Blob> layers;

    private String token;

    public Context(Reference reference, Blob config, List<Blob> layers) {
        this.reference = reference;
        this.config = config;
        this.layers = layers;
    }
    
    public ManifestFile manifestFile() {
        ManifestFile manifestFile = new ManifestFile();
        manifestFile.setConfig(getConfig().getName());
        manifestFile.setRepoTags(Collections.singletonList(getReference().toString()));
        manifestFile.setLayers(getLayers().stream().map(Blob::getName).collect(Collectors.toList()));
        return manifestFile;
    }

    public ManifestHttp manifestHttp() {
        ManifestHttp manifest = new ManifestHttp();
        manifest.setSchemaVersion(Constants.SCHEMA_V_2);
        manifest.setMediaType(ImageMediaType.MANIFEST_V2);
        manifest.setConfig(new ManifestHttp.BlobDTO(ImageMediaType.CONFIG, getConfig().getSize(), getConfig().getDigest()));
        manifest.setLayers(getLayers().stream().map(blob -> new ManifestHttp.BlobDTO(ImageMediaType.CONFIG, blob.getSize(),
                blob.getDigest())).collect(Collectors.toList()));
        return manifest;
    }
}
