package be.cytomine.appengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;

@Data
@EqualsAndHashCode(callSuper = false)
public class RunTaskServiceException extends Exception {

    private AppEngineError error;

    public RunTaskServiceException(AppEngineError error) {
        super();
        this.error = error;
    }

    public RunTaskServiceException(Exception e) {
        super(e);
    }

    public RunTaskServiceException(String message) {
        super(message);
    }
}
