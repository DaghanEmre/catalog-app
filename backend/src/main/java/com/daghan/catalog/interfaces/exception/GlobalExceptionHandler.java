package com.daghan.catalog.interfaces.exception;

import com.daghan.catalog.domain.exception.InvalidProductInputException;
import com.daghan.catalog.domain.exception.InvalidProductStateException;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints.
 * Converts exceptions to RFC 7807 Problem Detail responses.
 */
@RestControllerAdvice(basePackages = "com.daghan.catalog.interfaces.web.rest")
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Pre-created URI constants to avoid null-safety warnings and improve
        // performance
        private static final URI TYPE_VALIDATION_ERROR = URI.create("urn:problem-type:validation-error");
        private static final URI TYPE_ACCESS_DENIED = URI.create("urn:problem-type:access-denied");
        private static final URI TYPE_INVALID_INPUT = URI.create("urn:problem-type:invalid-input");
        private static final URI TYPE_STATE_CONFLICT = URI.create("urn:problem-type:state-conflict");
        private static final URI TYPE_NOT_FOUND = URI.create("urn:problem-type:not-found");
        private static final URI TYPE_INTERNAL_ERROR = URI.create("urn:problem-type:internal-error");

        private static final String TIMESTAMP_PROPERTY = "timestamp";

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
                // Use LinkedHashMap to preserve field order and handle duplicate fields
                Map<String, String> errors = new LinkedHashMap<>();
                for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                        String fieldName = fieldError.getField();
                        String message = fieldError.getDefaultMessage() != null
                                        ? fieldError.getDefaultMessage()
                                        : "Invalid value";
                        // If field already exists, append the new message
                        errors.merge(fieldName, message, (existing, newMsg) -> existing + "; " + newMsg);
                }

                log.debug("Validation failed for request: {}", errors);

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed");
                pd.setType(TYPE_VALIDATION_ERROR);
                pd.setTitle("Validation Error");
                pd.setProperty("errors", errors);
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.badRequest().body(pd);
        }

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
                log.warn("Access denied: {}", ex.getMessage());

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.FORBIDDEN,
                                "Access denied");
                pd.setType(TYPE_ACCESS_DENIED);
                pd.setTitle("Access Denied");
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
        }

        @ExceptionHandler(InvalidProductInputException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ProblemDetail> handleInvalidInput(InvalidProductInputException ex) {
                log.debug("Invalid product input: {}", ex.getMessage());

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage());
                pd.setType(TYPE_INVALID_INPUT);
                pd.setTitle("Invalid Input");
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.badRequest().body(pd);
        }

        @ExceptionHandler(InvalidProductStateException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ResponseEntity<ProblemDetail> handleInvalidState(InvalidProductStateException ex) {
                log.debug("Invalid product state transition: {}", ex.getMessage());

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                ex.getMessage());
                pd.setType(TYPE_STATE_CONFLICT);
                pd.setTitle("State Conflict");
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
        }

        @ExceptionHandler(ProductNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<ProblemDetail> handleNotFound(ProductNotFoundException ex) {
                log.debug("Product not found: {}", ex.getMessage());

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
                pd.setType(TYPE_NOT_FOUND);
                pd.setTitle("Resource Not Found");
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
                // Log the full exception for debugging - critical for production
                // troubleshooting
                log.error("Unexpected error occurred", ex);

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred");
                pd.setType(TYPE_INTERNAL_ERROR);
                pd.setTitle("Internal Server Error");
                pd.setProperty(TIMESTAMP_PROPERTY, Instant.now());
                return ResponseEntity.internalServerError().body(pd);
        }
}
