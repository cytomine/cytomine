package be.cytomine.controller.error;

import java.util.HashMap;
import java.util.Map;

public class ErrorBuilder {
    public static Error build(ErrorCode code, Map<String, String> details) {
        return ErrorsDictionary.get(code, details);
    }

    public static Error build(ErrorCode code) {
        return build(code, new HashMap<>());
    }
}
