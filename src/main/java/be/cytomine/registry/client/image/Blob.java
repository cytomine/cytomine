package io.github.ya_b.registry.client.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blob {

    // manifest.json中的名字。tar entry.name
    private String name;
    private Long size;
    private String digest;
    private Supplier<InputStream> content;
}
