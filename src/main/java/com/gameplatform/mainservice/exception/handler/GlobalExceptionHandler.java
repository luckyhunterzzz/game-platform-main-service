package com.gameplatform.mainservice.exception.handler;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.exception.exceptions.InvalidAuthenticationException;
import com.gameplatform.mainservice.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
            BusinessValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Business validation error: {} at {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthenticationException(
            InvalidAuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication error: {} at {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        log.warn("Request body validation failed: {} at {}", message, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("Constraint violation: {} at {}", message, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred at {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal server error", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    private String formatFieldError(FieldError fieldError) {
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();

        if (message == null || message.isBlank()) {
            message = "Validation failed";
        }

        return field + ": " + message;
    }
}