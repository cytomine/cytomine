package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * User: lrollus Date: 17/11/11 This exception means that the content of the request in not valid E.g. The project we
 * want to add has no ontology
 */
@Slf4j
public class InvalidRequestException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public InvalidRequestException(String message) {
        super(message, 400);
        log.warn(message);
    }

}
