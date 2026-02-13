package be.cytomine.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeUtils {

    public static Optional<String> optString(JsonNode root, String field) {
        if (root == null) return Optional.empty();

        JsonNode node = root.path(field);
        if (node.isMissingNode() || node.isNull()) return Optional.empty();

        String value = csvFromStringArrayNode(node);
        if (value == null) return Optional.empty();

        value = value.trim();
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    public static String csvFromStringArrayNode(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull() || arrayNode.isMissingNode()) return null;
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("Expected an array JsonNode but got: " + arrayNode.getNodeType());
        }

        List<String> parts = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (item == null || item.isNull() || item.isMissingNode()) continue;
            String v = item.asText(null);
            if (v == null) continue;
            v = v.trim();
            if (!v.isEmpty()) parts.add(v);
        }

        return parts.isEmpty() ? null : String.join(",", parts);
    }
}
