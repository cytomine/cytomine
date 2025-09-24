package com.cytomine.registry.client.http;

<<<<<<< HEAD
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

=======
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

>>>>>>> origin/main
@NoArgsConstructor
@Data
public class ErrorResponse {

    private List<ErrorsDTO> errors;

<<<<<<< HEAD
    @Override
    public String toString() {
        return errors.stream().map(ErrorsDTO::toString).collect(Collectors.joining("."));
    }

=======
>>>>>>> origin/main
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
<<<<<<< HEAD
=======

    @Override
    public String toString() {
        return errors.stream().map(ErrorsDTO::toString).collect(Collectors.joining("."));
    }
>>>>>>> origin/main
}
