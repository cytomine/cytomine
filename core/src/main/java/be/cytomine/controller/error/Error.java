package be.cytomine.controller.error;

import java.util.Map;

public record Error(
    String message,
    ErrorCode errorCode,
    Map<String, String> details
) {}
