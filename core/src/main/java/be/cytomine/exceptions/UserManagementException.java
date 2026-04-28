package be.cytomine.exceptions;

import lombok.extern.slf4j.Slf4j;

import be.cytomine.controller.error.ErrorCode;
import be.cytomine.controller.error.ErrorsDictionary;


@Slf4j
public class UserManagementException extends CytomineException {

    public ErrorCode errorCode;

    public UserManagementException(String message, int code, ErrorCode errorCode) {
        super(message, code);
        this.errorCode = errorCode;
        log.info(message);
    }

    public UserManagementException(int code, ErrorCode errorCode) {
        super(ErrorsDictionary.get(errorCode, null).message(), code);
        this.errorCode = errorCode;
        log.info(ErrorsDictionary.get(errorCode, null).message());
    }

    public UserManagementException(String message, int code) {
        super(message, code);
    }
}
