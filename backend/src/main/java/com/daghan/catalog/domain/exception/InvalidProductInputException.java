package com.daghan.catalog.domain.exception;

public class InvalidProductInputException extends RuntimeException {
    public InvalidProductInputException(String message) {
        super(message);
    }
}
