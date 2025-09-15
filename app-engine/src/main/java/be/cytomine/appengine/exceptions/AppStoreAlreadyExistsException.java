package be.cytomine.appengine.exceptions;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;

public class AppStoreAlreadyExistsException extends Exception {

    private AppEngineError error;

    public AppStoreAlreadyExistsException(Exception e) {
        super(e);
    }

    public AppStoreAlreadyExistsException(String message) {
        super(message);
    }

    public AppStoreAlreadyExistsException(AppEngineError error) {
        super(error.getMessage());
        this.error = error;
    }
}
