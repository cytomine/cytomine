package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * User: lrollus Date: 17/11/11 This exception means that the object was not found on DB It correspond to the HTTP code
 * 404
 */
@Slf4j
public class ObjectNotFoundException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public ObjectNotFoundException(String message) {
        super(message, 404);
        log.warn(message);
    }

    public ObjectNotFoundException(String objectType, Object objectId) {
        super(objectType + " " + objectId + " not found", 404);
        log.warn(super.getMessage());
    }

    public ObjectNotFoundException(String objectType, String objectId) {
        super(objectType + " " + objectId + " not found", 404);
        log.warn(super.getMessage());
    }

    public ObjectNotFoundException(String objectType, Long objectId) {
        this(objectType, String.valueOf(objectId));
    }
}
