package com.cytomine.registry.client.image;

<<<<<<< HEAD
import java.io.InputStream;

=======
>>>>>>> origin/main
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
=======
import java.io.InputStream;

>>>>>>> origin/main
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
