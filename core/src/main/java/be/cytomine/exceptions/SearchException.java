package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public String body;

    public SearchException(String message, int httpCode, String body) {
        super(message, httpCode);
        this.body = body;
    }

}
