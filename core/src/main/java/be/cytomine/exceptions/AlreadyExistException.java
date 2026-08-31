package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * User: lrollus Date: 17/11/11 This exception means that a domain already exist in database For exemple: we try to add
 * a project with same name It correspond to the HTTP code 409 (Conflict)
 */
@Slf4j
public class AlreadyExistException extends CytomineException {

    public static int CODE = 409;

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public AlreadyExistException(String message) {
        super(message, CODE);
        log.info(message);
    }
}
