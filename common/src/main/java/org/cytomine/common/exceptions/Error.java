package org.cytomine.common.exceptions;

import java.util.Map;

import org.cytomine.common.exceptions.ErrorCode;

public record Error(
    String message,
    ErrorCode errorCode,
    Map<String, String> details
) {
    public static Error of(ErrorCode code, Map<String, String> details) {
        return new Error(code.getMessage(), code, details);
    }
}
