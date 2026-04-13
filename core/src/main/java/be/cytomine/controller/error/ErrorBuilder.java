package be.cytomine.controller.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ErrorBuilder {
    public static JsonNode build(ErrorCode code, Map<String, String> details) {
        ObjectMapper mapper = new ObjectMapper();
        Error error = ErrorsDictionary.get(code);
        error.setDetails(details);
        return mapper.valueToTree(error);
    }

    public static JsonNode build(ErrorCode code) {
        return build(code, new HashMap<>());
    }
}
