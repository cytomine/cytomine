package be.cytomine.controller.error;

import java.util.Map;

public record Error(
    String message,
    ErrorCode errorCode,
    Map<String, String> details
) {
    public static Error of(ErrorCode code, Map<String, String> details) {
        return new Error(code.getMessage(), code, details);
    }
}
