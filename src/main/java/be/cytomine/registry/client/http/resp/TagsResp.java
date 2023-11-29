package io.github.ya_b.registry.client.http.resp;

import lombok.Data;

import java.util.List;

@Data
public class TagsResp {
    private String name;
    private List<String> tags;
}
