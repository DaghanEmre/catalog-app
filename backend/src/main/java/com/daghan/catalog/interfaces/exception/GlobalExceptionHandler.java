package com.daghan.catalog.interfaces.exception;

import com.daghan.catalog.domain.exception.InvalidProductInputException;
import com.daghan.catalog.domain.exception.InvalidProductStateException;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.daghan.catalog.interfaces.web.rest")
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                fieldError -> fieldError.getDefaultMessage() != null
                                                                ? fieldError.getDefaultMessage()
                                                                : "Invalid value"));

                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed");
                pd.setType(URI.create("urn:problem-type:validation-error"));
                pd.setTitle("Validation Error");
                pd.setProperty("errors", errors);
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.FORBIDDEN,
                                "Access denied");
                pd.setType(URI.create("urn:problem-type:access-denied"));
                pd.setTitle("Access Denied");
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }

        @ExceptionHandler(InvalidProductInputException.class)
        public ProblemDetail handleInvalidInput(InvalidProductInputException ex) {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage());
                pd.setType(URI.create("urn:problem-type:invalid-input"));
                pd.setTitle("Invalid Input");
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }

        @ExceptionHandler(InvalidProductStateException.class)
        public ProblemDetail handleInvalidState(InvalidProductStateException ex) {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                ex.getMessage());
                pd.setType(URI.create("urn:problem-type:state-conflict"));
                pd.setTitle("State Conflict");
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }

        @ExceptionHandler(ProductNotFoundException.class)
        public ProblemDetail handleNotFound(ProductNotFoundException ex) {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
                pd.setType(URI.create("urn:problem-type:not-found"));
                pd.setTitle("Resource Not Found");
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }

        @ExceptionHandler(Exception.class)
        public ProblemDetail handleGenericException(Exception ex) {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred");
                pd.setType(URI.create("urn:problem-type:internal-error"));
                pd.setTitle("Internal Server Error");
                pd.setProperty("timestamp", Instant.now());
                return pd;
        }
}
