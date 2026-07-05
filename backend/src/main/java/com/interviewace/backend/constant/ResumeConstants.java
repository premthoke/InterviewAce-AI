package com.interviewace.backend.constant;

/**
 * Shared constants for the Resume domain.
 *
 * <p>Centralizes validation thresholds and MIME types so they are never
 * scattered as magic strings or numbers across services and controllers.</p>
 */
public final class ResumeConstants {

    /** Maximum allowed file size for resume uploads: 5 MB. */
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** Allowed MIME type for resume uploads (Phase 1 — PDF only). */
    public static final String ALLOWED_MIME_TYPE = "application/pdf";

    /** Human-readable file size limit for error messages. */
    public static final String MAX_FILE_SIZE_DISPLAY = "5 MB";

    private ResumeConstants() {
        // Prevent instantiation
    }

}
