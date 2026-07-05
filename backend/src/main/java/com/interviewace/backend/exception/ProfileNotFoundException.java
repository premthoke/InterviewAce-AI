package com.interviewace.backend.exception;

/**
 * Thrown when a profile lookup fails because the authenticated user
 * has not yet created a profile.
 *
 * <p>Handled by {@link GlobalExceptionHandler} to return HTTP 404.</p>
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String message) {
        super(message);
    }

}
