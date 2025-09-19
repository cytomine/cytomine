package com.cytomine.registry.client.exception;

import java.io.IOException;

public class RegistryException extends IOException {

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(Throwable cause) {
        super(cause);
    }
}