package be.cytomine.appengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppStoreServiceException extends Exception {

    private AppEngineError error;

    public AppStoreServiceException(AppEngineError error) {
        super();
        this.error = error;
    }

    public AppStoreServiceException(Exception e) {
        super(e);
    }

    public AppStoreServiceException(String message) {
        super(message);
    }
}