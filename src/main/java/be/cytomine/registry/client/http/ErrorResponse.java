package io.github.ya_b.registry.client.http;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class ErrorResponse {

    private List<ErrorsDTO> errors;

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

    @Override
    public String toString() {
        return errors.stream().map(ErrorsDTO::toString).collect(Collectors.joining("."));
    }
}
