package com.revoktek.services.controller;


import com.revoktek.services.rulesException.DuplicateModelException;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.rulesException.UniqueFieldException;
import com.revoktek.services.utils.Message;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Message> IllegalStateException(IllegalStateException ise) {
        log.error("An error occurred while processing the request", ise);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(false, ise.getMessage() ,""));
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> methodNotFoundException() {
        log.error("Method not found exception");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(false, "MethodNotFoundException", "Method not found"));
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ResponseEntity<?> usernameNotFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(false, "UsernameNotFoundException", "User not found"));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<?> accessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(false, "AccessDeniedException", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.add(new FieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()
                ))
        );
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false, "MethodArgumentNotValidException", fieldErrors));
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<?> methodArgumentNotValidException(ConstraintViolationException ex) {
        List<FieldError> fieldErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(constraintViolation -> fieldErrors.add(new FieldError(constraintViolation.getPropertyPath().toString().split("\\.")[1], constraintViolation.getMessage(), constraintViolation.getInvalidValue())));
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false, "MethodParamsNotValidException", fieldErrors));
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<?> bindException(BindException ex) {
        List<FieldError> fieldErrors = new ArrayList<>();
        ex.getFieldErrors().forEach(fieldError -> fieldErrors.add(new FieldError(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue())));
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false, "MethodArgumentDTONotValidException", fieldErrors));
    }

    @ExceptionHandler(DuplicateModelException.class)
    public ResponseEntity<?> duplicateModelException(DuplicateModelException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(false, e.getMessage(), ""));
    }

    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<Message> NotFoundException(ModelNotFoundException mnfe) {
        log.error("An error occurred while processing the request", mnfe);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(false, mnfe.getMessage() ,""));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalException(Exception exception) {
        log.error("An error occurred while processing the request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Message(false, "GlobalException", "An error occurred while processing the request"));
    }


    @ExceptionHandler(UniqueFieldException.class)
    public ResponseEntity<Message> UniqueFieldException(UniqueFieldException ufe) {
        log.error("An error occurred while processing the request", ufe);
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false,ufe.getMessage() ,""));
    }

    @ExceptionHandler(EnumInvalidArgumentException.class)
    public ResponseEntity<?> enumInvalidArgumentException(EnumInvalidArgumentException e) {
        log.error("Invalid format exception", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(false, e.getMessage() ,""));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingParameterException(MissingServletRequestParameterException e) {
        log.error("An error occurred while processing the request", e);
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false, e.getMessage() ,""));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<?> numberFormatException(NumberFormatException e) {
        log.error("Number format exception", e);
        return ResponseEntity.status(HttpStatus.OK).body(new Message(false,e.getMessage(),""));
    }


    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<?> authenticationServiceException(AuthenticationServiceException e) {
        log.error("Authentication service exception", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(false, "AuthenticationServiceException", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return ResponseEntity.badRequest().body(new Message(false, e.getMessage(), "IllegalArgumentException"));
    }

    @Getter
    @Setter
    public static class FieldError {
        private String field;
        private String message;
        private Object valueRejected;

        public FieldError(String field, String message, Object valueRejected) {
            this.field = field;
            this.message = message;
            this.valueRejected = valueRejected;
        }

        @Override
        public String toString() {
            return "FieldError{" +
                    "field='" + field +
                    ", message='" + message +
                    ", valueRejected=" + valueRejected +
                    '}';
        }
    }

}

