package be.cytomine.controller.error;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    private String message;
    private String errorCode;
    private Map<String, String> details;

    public Error(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
