package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiddlewareException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public MiddlewareException(String message) {
        super(message, 500);
        log.error(message);
    }

}
