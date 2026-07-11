package com.interviewace.backend.exception;

/**
 * Thrown when the external storage provider (e.g., Cloudinary) fails
 * to upload or delete a resume file.
 *
 * <p>This exception wraps the underlying SDK exception and provides
 * a clean application-layer error. It ensures that storage provider
 * implementation details do not leak into the controller layer.</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to return HTTP 500.</p>
 */
public class StorageUploadException extends RuntimeException {

    public StorageUploadException(String message) {
        super(message);
    }

    public StorageUploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
