package com.cytomine.registry.client.image;


<<<<<<< HEAD
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

=======
>>>>>>> origin/main
import com.cytomine.registry.client.constant.Constants;
import com.cytomine.registry.client.image.registry.ManifestHttp;
import com.cytomine.registry.client.image.tar.ManifestFile;
import com.cytomine.registry.client.name.Reference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

<<<<<<< HEAD
=======
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

>>>>>>> origin/main
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
<<<<<<< HEAD

=======
    
>>>>>>> origin/main
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
<<<<<<< HEAD
        manifest.setConfig(new ManifestHttp.BlobDTO(ImageMediaType.CONFIG, getConfig().getSize(),
            getConfig().getDigest()));
        manifest.setLayers(getLayers().stream().map(blob -> new ManifestHttp.BlobDTO(ImageMediaType.LAYER, blob.getSize(),
            blob.getDigest())).collect(Collectors.toList()));
=======
        manifest.setConfig(new ManifestHttp.BlobDTO(ImageMediaType.CONFIG, getConfig().getSize(), getConfig().getDigest()));
        manifest.setLayers(getLayers().stream().map(blob -> new ManifestHttp.BlobDTO(ImageMediaType.LAYER, blob.getSize(),
                blob.getDigest())).collect(Collectors.toList()));
>>>>>>> origin/main
        return manifest;
    }
}
