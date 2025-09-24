package com.cytomine.registry.client.image.registry;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> origin/main
import com.cytomine.registry.client.image.ImageMediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> origin/main
@NoArgsConstructor
@Data
public class ManifestHttp {

    private Integer schemaVersion;
    private ImageMediaType mediaType;
    private BlobDTO config;
    private List<BlobDTO> layers;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class BlobDTO {
        private ImageMediaType mediaType;
        private Long size;
        private String digest;
    }
}
