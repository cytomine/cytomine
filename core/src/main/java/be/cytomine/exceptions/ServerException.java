package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * User: lrollus Date: 17/11/11 This exception means that the server failed
 */
@Slf4j
public class ServerException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public ServerException(String message) {
        super(message, 500);
        log.error(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, 500, cause);
        log.error(message, cause);
    }
}
