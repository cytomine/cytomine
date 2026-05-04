package be.cytomine.service.utils;

import org.springframework.http.ResponseEntity;

public record Validation(
    boolean ok,
    ResponseEntity<?> responseEntity
) {}
