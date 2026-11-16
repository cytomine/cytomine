package org.cytomine.common.exceptions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

/**
 * User: lrollus Date: 17/11/11 This exception is the top exception for all cytomine exception It store a message and a
 * code, corresponding to an HTTP code
 */
public abstract class CytomineException extends RuntimeException {


    /**
     * Http code for an exception
     */
    public int code;

    /**
     * Message for exception
     */
    public String msg;

    /**
     * Values of the exception
     */
    @Getter
    public Map<Object, Object> values;


    /**
     * Headers to be sent in the response (can contain information on the exception or how the client has to treat the
     * exception).
     */
    @Getter
    public Map<String, String> headers;

    /**
     * Message map with this exception
     *
     * @param msg  Message
     * @param code Http code
     */
    public CytomineException(String msg, int code) {
        this(msg, code, new HashMap<>(), new LinkedHashMap<>(), null);
    }

    public CytomineException(String msg, int code, Throwable cause) {
        this(msg, code, new HashMap<>(), new LinkedHashMap<>(), cause);
    }


    public CytomineException(String msg, int code, Map<Object, Object> values) {
        this(msg, code, values, new LinkedHashMap<>(), null);
    }

    public CytomineException(
        String msg,
        int code,
        Map<Object, Object> values,
        Map<String, String> headers,
        Throwable cause
    ) {
        super(msg, cause);
        this.msg = msg;
        this.code = code;
        this.values = values;
        this.headers = headers;
    }

    public String toString() {
        return this.msg;
    }

}
