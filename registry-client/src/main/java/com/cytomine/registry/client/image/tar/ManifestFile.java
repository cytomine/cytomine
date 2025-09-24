package com.cytomine.registry.client.image.tar;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

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
