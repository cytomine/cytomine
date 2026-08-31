package be.cytomine.exceptions;

import java.util.Map;

public class NotModifiedException extends CytomineException {
    public NotModifiedException(Map<String, String> headers) {
        super(null, 304, null, headers, null);
    }
}
