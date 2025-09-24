package com.cytomine.registry.client.image.tar;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> origin/main
import com.cytomine.registry.client.image.ImageMediaType;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> origin/main
@NoArgsConstructor
@Data
public class IndexFile {

    private Integer schemaVersion;
    private ImageMediaType mediaType;
    private List<ManifestsDTO> manifests;

    @NoArgsConstructor
    @Data
    public static class ManifestsDTO {
        private ImageMediaType mediaType;
        private String digest;
        private Long size;
        private AnnotationsDTO annotations;

        @NoArgsConstructor
        @Data
        public static class AnnotationsDTO {
            @SerializedName("org.opencontainers.image.ref.name")
            private String imageRefName;
        }
    }
}
