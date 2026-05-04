package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

import be.cytomine.controller.error.ErrorCode;


@Slf4j
public class UserManagementException extends CytomineException {

    public ErrorCode errorCode;

    public UserManagementException(String message, int code, ErrorCode errorCode) {
        super(message, code);
        this.errorCode = errorCode;
        log.info(message);
    }

    public UserManagementException(int code, ErrorCode errorCode) {
        super(errorCode.getMessage(), code);
        this.errorCode = errorCode;
        log.info(errorCode.getMessage());
    }

    public UserManagementException(String message, int code) {
        super(message, code);
    }
}
