package io.github.ya_b.registry.client.image.registry;

import io.github.ya_b.registry.client.image.ImageMediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
