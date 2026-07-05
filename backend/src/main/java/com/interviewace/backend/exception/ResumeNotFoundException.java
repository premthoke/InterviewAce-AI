package com.interviewace.backend.exception;

/**
 * Thrown when a resume lookup fails because the authenticated user
 * has not yet uploaded a resume.
 *
 * <p>Since the Resume aggregate is created lazily (only on first upload),
 * this exception is the expected response for any user who has not
 * uploaded a resume yet.</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to return HTTP 404.</p>
 */
public class ResumeNotFoundException extends RuntimeException {

    public ResumeNotFoundException(String message) {
        super(message);
    }

}
