package com.interviewace.backend.exception;

import com.interviewace.backend.enums.ParseFailureReason;

/**
 * Thrown when PDF text extraction fails at any stage of the parsing pipeline.
 *
 * <p>This exception carries a structured {@link ParseFailureReason} that
 * classifies the failure for dashboards, analytics, and retry decisions,
 * in addition to the standard {@code message} and {@code cause} for
 * detailed debugging.</p>
 *
 * <p><b>Usage patterns:</b></p>
 * <ul>
 *     <li><b>Automatic (fire-and-forget) flow:</b> Caught internally by
 *         {@code PdfParserServiceImpl} — the failure reason and message are
 *         persisted to the database. The exception is never propagated.</li>
 *     <li><b>Manual retry endpoint:</b> Thrown from the {@code /parse} endpoint
 *         and caught by {@link GlobalExceptionHandler} to return HTTP 500.</li>
 * </ul>
 *
 * @see ParseFailureReason
 * @see GlobalExceptionHandler
 */
public class PdfParseException extends RuntimeException {

    /**
     * The structured reason category for this parsing failure.
     */
    private final ParseFailureReason failureReason;

    /**
     * Creates a new PdfParseException with a failure reason and message.
     *
     * @param failureReason the structured failure category
     * @param message       the detailed error description
     */
    public PdfParseException(ParseFailureReason failureReason, String message) {
        super(message);
        this.failureReason = failureReason;
    }

    /**
     * Creates a new PdfParseException with a failure reason, message, and cause.
     *
     * @param failureReason the structured failure category
     * @param message       the detailed error description
     * @param cause         the underlying exception that caused the failure
     */
    public PdfParseException(ParseFailureReason failureReason, String message, Throwable cause) {
        super(message, cause);
        this.failureReason = failureReason;
    }

    /**
     * Returns the structured failure reason for this parsing error.
     *
     * @return the failure reason category
     */
    public ParseFailureReason getFailureReason() {
        return failureReason;
    }
}
