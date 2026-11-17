package org.cytomine.common.beans.exception.http;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.common.exceptions.AlreadyExistException;
import org.cytomine.common.exceptions.AppEngineException;
import org.cytomine.common.exceptions.AuthenticationException;
import org.cytomine.common.exceptions.ConstraintException;
import org.cytomine.common.exceptions.CytomineMethodNotYetImplementedException;
import org.cytomine.common.exceptions.ForbiddenException;
import org.cytomine.common.exceptions.MiddlewareException;
import org.cytomine.common.exceptions.NotModifiedException;
import org.cytomine.common.exceptions.ObjectNotFoundException;
import org.cytomine.common.exceptions.SearchException;
import org.cytomine.common.exceptions.UserManagementException;
import org.cytomine.common.exceptions.WrongArgumentException;
import org.cytomine.common.exceptions.payload.CytomineHttpError;
import org.cytomine.common.exceptions.payload.CytomineHttpErrorAndValues;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        CytomineHttpError cytomineHttpError = CytomineHttpError.of(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cytomineHttpError);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Request refused because body cannot be parsed:" + ex, ex);
        CytomineHttpError cytomineHttpError =
            CytomineHttpError.of("The body cannot be parsed, is the body content in the good format?");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cytomineHttpError);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        CytomineHttpError cytomineHttpError = CytomineHttpError.of(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cytomineHttpError);
    }

    @ExceptionHandler(MiddlewareException.class)
    public ResponseEntity<?> handleException(MiddlewareException exception) {
        CytomineHttpErrorAndValues cytomineHttpError =
            CytomineHttpErrorAndValues.of("Internal error", exception.getValues());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cytomineHttpError);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<?> handleException(ObjectNotFoundException exception) {
        CytomineHttpErrorAndValues cytomineHttpError =
            CytomineHttpErrorAndValues.of(exception.getMessage(), exception.getValues());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cytomineHttpError);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleException(ForbiddenException exception) {
        CytomineHttpErrorAndValues cytomineHttpError =
            CytomineHttpErrorAndValues.of(exception.getMessage(), exception.getValues());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(cytomineHttpError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleException(AccessDeniedException exception) {
        CytomineHttpError cytomineHttpError =
            CytomineHttpError.of("Cannot identify user or user is not authorized to log in");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(cytomineHttpError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleException(AuthenticationException exception) {
        CytomineHttpError cytomineHttpError = CytomineHttpError.of(exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(cytomineHttpError);
    }

    @ExceptionHandler(CytomineMethodNotYetImplementedException.class)
    public ResponseEntity<?> handleException(CytomineMethodNotYetImplementedException exception) {
        CytomineHttpError cytomineHttpError = CytomineHttpError.of("Method is not yet implemented");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cytomineHttpError);
    }

    @ExceptionHandler(WrongArgumentException.class)
    public ResponseEntity<?> handleException(WrongArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CytomineHttpErrorAndValues.of(exception.getMessage(), exception.getValues()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> handleException(AlreadyExistException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CytomineHttpErrorAndValues.of(exception.getMessage(), exception.getValues()));
    }

    @ExceptionHandler(ConstraintException.class)
    public ResponseEntity<?> handleException(ConstraintException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CytomineHttpErrorAndValues.of(exception.getMessage(), exception.getValues()));
    }

    @ExceptionHandler(NotModifiedException.class)
    public ResponseEntity<?> handleException(NotModifiedException exception) {
        log.debug("NotModifiedException");
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : exception.getHeaders().entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
    }

    @ExceptionHandler(AppEngineException.class)
    public ResponseEntity<?> handleException(AppEngineException exception) {
        log.debug("AppEngineException");
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : exception.getHeaders().entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return new ResponseEntity<>(exception.body, headers, HttpStatus.valueOf(exception.code));
    }

    @ExceptionHandler(SearchException.class)
    public ResponseEntity<?> handleException(SearchException exception) {
        log.error("Error (code {}): {}", exception.code, exception.body, exception);
        CytomineHttpError cytomineHttpError = CytomineHttpError.of(exception.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(exception.code)).body(cytomineHttpError);
    }

    @ExceptionHandler(UserManagementException.class)
    public ResponseEntity<?> handleException(UserManagementException exception) {
        log.error("Error (code {}): {}", exception.code, exception.getMessage(), exception);
        CytomineHttpError cytomineHttpError = CytomineHttpError.of(exception.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(exception.code)).body(cytomineHttpError);
    }
}
