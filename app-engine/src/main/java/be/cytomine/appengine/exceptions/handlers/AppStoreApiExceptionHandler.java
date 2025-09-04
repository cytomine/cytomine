package be.cytomine.appengine.exceptions.handlers;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.AppStoreServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class AppStoreApiExceptionHandler {
    @ExceptionHandler({ AppStoreServiceException.class })
    public final ResponseEntity<AppEngineError> handleException(Exception e) {
        AppEngineError error = ErrorBuilder.build(ErrorCode.APPSTORE_DOWNLOAD_FAILED);
        log.info("bad request 400 error: {}", error.getMessage());
        return new ResponseEntity<AppEngineError>(error, HttpStatus.BAD_REQUEST);
    }

}
