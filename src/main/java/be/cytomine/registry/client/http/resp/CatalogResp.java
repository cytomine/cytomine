package io.github.ya_b.registry.client.http.resp;

import lombok.Data;

import java.util.List;

@Data
public class CatalogResp {

    private List<String> repositories;

    private String next;
}
