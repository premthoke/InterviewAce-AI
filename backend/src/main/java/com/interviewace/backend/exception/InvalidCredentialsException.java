package com.interviewace.backend.exception;

/**
 * Thrown when authentication fails due to invalid email or password.
 * Handled by GlobalExceptionHandler to return HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

}
