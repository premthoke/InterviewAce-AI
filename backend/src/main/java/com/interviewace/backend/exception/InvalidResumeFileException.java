package com.interviewace.backend.exception;

/**
 * Thrown when a resume file fails validation checks.
 *
 * <p>Validation failures include:</p>
 * <ul>
 *     <li>File exceeds maximum size (5MB)</li>
 *     <li>Invalid MIME type (only {@code application/pdf} allowed)</li>
 *     <li>Invalid file extension (only {@code .pdf} allowed)</li>
 *     <li>Empty or missing file</li>
 * </ul>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to return HTTP 400.</p>
 */
public class InvalidResumeFileException extends RuntimeException {

    public InvalidResumeFileException(String message) {
        super(message);
    }

}
