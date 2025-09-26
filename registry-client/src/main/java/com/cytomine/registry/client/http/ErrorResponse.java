package com.cytomine.registry.client.http;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ErrorResponse {

    private List<ErrorsDTO> errors;

    @Override
    public String toString() {
        return errors.stream().map(ErrorsDTO::toString).collect(Collectors.joining("."));
    }

    @NoArgsConstructor
    @Data
    public static class ErrorsDTO {
        private String code;
        private String message;

        @Override
        public String toString() {
            return String.format("code: %s, message: %s", code, message);
        }
    }
}
