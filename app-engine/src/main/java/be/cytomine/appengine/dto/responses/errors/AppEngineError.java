package be.cytomine.appengine.dto.responses.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

import be.cytomine.appengine.dto.responses.errors.details.BaseErrorDetails;

@Data
@AllArgsConstructor
public class AppEngineError {
    private String errorCode;

    private String message;

    private BaseErrorDetails details;
}
