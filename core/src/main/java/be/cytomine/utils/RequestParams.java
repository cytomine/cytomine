package be.cytomine.utils;

import java.util.HashMap;

public class RequestParams extends HashMap<String, String> {

    public boolean isTrue(String key) {
        return !isNull(key) && get(key).equals("true");
    }

    public boolean isNull(String key) {
        return get(key) == null;
    }

    public boolean getWithImageGroup() {
        return get("withImageGroup").equals("true");
    }

    public Long getOffset() {
        return Long.parseLong(get("offset"));
    }

    public Long getMax() {
        return Long.parseLong(get("max"));
    }

    public String getSort() {
        return get("sort");
    }

    public String getOrder() {
        return get("order");
    }
}
