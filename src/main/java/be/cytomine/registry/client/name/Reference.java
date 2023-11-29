package io.github.ya_b.registry.client.name;

import io.github.ya_b.registry.client.constant.Constants;
import io.github.ya_b.registry.client.http.HttpClient;
import io.github.ya_b.registry.client.manager.RegistryManager;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Reference {
    private static final int DIGEST_LENGTH = 71;
    private static final RegistryManager REGISTRY_MANAGER = new RegistryManager();
    private String endpoint;
    private String name;
    private String tag;
    private String digest;

    public static Reference parse(String image) {
        Reference t = new Reference();
        List<String> list = new ArrayList<>(Arrays.asList(image.split(Constants.SEPARATOR)));
        String host;
        if (list.size() > 1 && (list.get(0).contains(Constants.COLON) || list.get(0).contains(Constants.DOT))) {
            host = list.remove(0);
        } else {
            host = Constants.ENDPOINT_DEFAULT;
        }
        t.endpoint = String.format("%s://%s", REGISTRY_MANAGER.getSchema(host), host);
        String last = list.get(list.size() - 1);
        if (last.contains(Constants.AT)) {
            int atIndex = last.lastIndexOf(Constants.AT);
            t.digest = last.substring(atIndex + 1);
            if (t.digest.length() != DIGEST_LENGTH || !t.digest.contains(Constants.SHA256_PREFIX)) {
                throw new IllegalArgumentException("digest format error");
            }
            last = last.substring(0, atIndex);
        }
        if (last.contains(Constants.COLON)) {
            int colonIndex = last.indexOf(Constants.COLON);
            t.tag = last.substring(colonIndex + 1);
            last = last.substring(0, colonIndex);
        } else {
            t.tag = Constants.TAG_LATEST;
        }
        list.set(list.size() - 1, last);
        if (t.endpoint.endsWith(Constants.ENDPOINT_DEFAULT) && list.size() == 1) {
            list.add(0, Constants.GROUP_DEFAULT);
        }
        t.name = String.join(Constants.SEPARATOR, list);
        return t;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (endpoint != null && !endpoint.isEmpty()) {
            stringBuilder.append(endpoint.replaceAll(HttpClient.URL_PREFIX, ""));
            stringBuilder.append(Constants.SEPARATOR);
        }
        if (name != null && !name.isEmpty()) {
            stringBuilder.append(name);
            stringBuilder.append(Constants.COLON);
        }
        if (tag != null && !tag.isEmpty()) {
            stringBuilder.append(tag);
        } else {
            stringBuilder.append(Constants.TAG_LATEST);
        }
        return stringBuilder.toString();
    }
}
