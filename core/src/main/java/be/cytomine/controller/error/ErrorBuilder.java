package be.cytomine.controller.error;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorBuilder {
    public static JsonNode build(ErrorCode code, Map<String, String> details) {
        ObjectMapper mapper = new ObjectMapper();
        Error error = ErrorsDictionary.get(code, details);
        return mapper.valueToTree(error);
    }

    public static JsonNode build(ErrorCode code) {
        return build(code, new HashMap<>());
    }
}
