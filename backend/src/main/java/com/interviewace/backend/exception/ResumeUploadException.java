package com.interviewace.backend.exception;

/**
 * Thrown when the resume upload workflow fails after the file has been
 * successfully stored but the database persistence step encounters an error.
 *
 * <p>When this exception is thrown, the workflow service has already
 * attempted a compensating cleanup (deleting the orphaned file from
 * the storage provider).</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to return HTTP 500.</p>
 */
public class ResumeUploadException extends RuntimeException {

    public ResumeUploadException(String message) {
        super(message);
    }

    public ResumeUploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
