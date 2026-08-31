package be.cytomine.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public AuthenticationException(String message) {
        this(message, new LinkedHashMap<>());
    }

    public AuthenticationException(String message, Map<Object, Object> values) {
        super(message, 401, values);
        log.warn(message);
    }

}
