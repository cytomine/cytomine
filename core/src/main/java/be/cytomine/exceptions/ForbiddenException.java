package be.cytomine.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForbiddenException extends CytomineException {

    /**
     * Message map with this exception
     *
     * @param message Message
     */
    public ForbiddenException(String message) {
        this(message, new LinkedHashMap<>());
    }

    public ForbiddenException(String message, Map<Object, Object> values) {
        super(message, 403, values);
    }

}
