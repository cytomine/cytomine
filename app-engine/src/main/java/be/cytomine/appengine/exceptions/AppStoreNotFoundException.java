package be.cytomine.appengine.exceptions;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;

public class AppStoreNotFoundException extends Exception {

    private AppEngineError error;

    public AppStoreNotFoundException(Exception e) {
        super(e);
    }

    public AppStoreNotFoundException(String message) {
        super(message);
    }

    public AppStoreNotFoundException(AppEngineError error) {
        super(error.getMessage());
        this.error = error;
    }
}
