package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * User: lrollus Date: 17/11/11 This exception means that some argument from request are not valid
 */
@Slf4j
public class WrongArgumentException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public WrongArgumentException(String message) {
        super(message, 400);
        log.warn(message);
    }
}
