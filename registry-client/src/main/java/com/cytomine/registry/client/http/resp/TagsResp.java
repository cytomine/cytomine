package com.cytomine.registry.client.http.resp;

import java.util.List;

import lombok.Data;

@Data
public class TagsResp {
    private String name;
    private List<String> tags;
}
