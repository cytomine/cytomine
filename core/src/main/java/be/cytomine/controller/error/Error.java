package be.cytomine.controller.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    private String message;
    private String error_code;
    private Map<String, String> details;

    public Error(String error_code, String message) {
        this.error_code = error_code;
        this.message = message;
    }
}
