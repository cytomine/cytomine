package com.cytomine.registry.client.image.tar;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> origin/main
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> origin/main
@NoArgsConstructor
@Data
public class ManifestFile {

    @SerializedName("Config")
    private String config;
    @SerializedName("RepoTags")
    private List<String> repoTags;
    @SerializedName("Layers")
    private List<String> layers;
}
