package be.cytomine.appengine.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.AppStoreAlreadyExistsException;
import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.exceptions.AppStoreValidationException;

@Slf4j
@ControllerAdvice
@Order(0)
public class AppStoreApiExceptionHandler {
    @ExceptionHandler({ AppStoreValidationException.class })
    public final ResponseEntity<AppEngineError> handleAppStoreException(Exception e) {
        AppEngineError error = ErrorBuilder.build(ErrorCode.INTERNAL_INVALID_STORE_DATA);
        log.info("bad request 400 error [{}]", e.getMessage());
        return new ResponseEntity<AppEngineError>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ AppStoreNotFoundException.class })
    public final ResponseEntity<AppEngineError> handleAppStoreNotFoundException(Exception e) {
        AppEngineError error = ErrorBuilder.build(ErrorCode.INTERNAL_INVALID_STORE_NOT_FOUND);
        log.info("bad request 404 error [{}]", e.getMessage());
        return new ResponseEntity<AppEngineError>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ AppStoreAlreadyExistsException.class })
    public final ResponseEntity<AppEngineError> handleAppStoreExistsException(Exception e) {
        AppEngineError error = ErrorBuilder.build(ErrorCode.INTERNAL_INVALID_STORE_NOT_FOUND);
        log.info("bad request 400 error [{}]", e.getMessage());
        return new ResponseEntity<AppEngineError>(error, HttpStatus.BAD_REQUEST);
    }

}
