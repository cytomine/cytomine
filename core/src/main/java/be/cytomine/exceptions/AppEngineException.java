package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppEngineException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public String body;
    public AppEngineException(String message, int httpCode, String body) {
        super(message, httpCode);
        this.body = body;
    }

}
