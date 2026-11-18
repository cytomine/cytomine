package org.cytomine.common.exceptions.payload;

import java.util.Map;

public record CytomineHttpErrorAndValues(HttpErrorMessage errors,
                                         Map<Object, Object> errorValues) {

    public static CytomineHttpErrorAndValues of(String message, Map<Object, Object> values) {
        return new CytomineHttpErrorAndValues(new HttpErrorMessage(message), values);
    }

}
