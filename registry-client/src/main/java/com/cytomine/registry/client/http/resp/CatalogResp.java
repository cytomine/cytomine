package com.cytomine.registry.client.http.resp;

import java.util.List;

import lombok.Data;

@Data
public class CatalogResp {

    private List<String> repositories;

    private String next;
}
