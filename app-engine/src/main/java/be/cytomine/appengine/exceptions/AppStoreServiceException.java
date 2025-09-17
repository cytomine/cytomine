package be.cytomine.appengine.exceptions;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import lombok.Data;
import lombok.EqualsAndHashCode;

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