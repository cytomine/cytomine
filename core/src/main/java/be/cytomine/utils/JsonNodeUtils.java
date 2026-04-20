package be.cytomine.utils;

import java.util.ArrayList;
import java.util.List;

public class JsonNodeUtils {

    public static String csvFromStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        for (String v : values) {
            if (v == null) {
                continue;
            }
            String s = v.trim();
            if (!s.isEmpty()) {
                parts.add(s);
            }
        }
        return parts.isEmpty() ? null : String.join(",", parts);
    }
}
