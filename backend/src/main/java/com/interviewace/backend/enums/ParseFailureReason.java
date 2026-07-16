package com.interviewace.backend.enums;

/**
 * Enumeration representing the structured reason why PDF parsing failed.
 *
 * <p>Provides a machine-readable failure category for dashboards, analytics,
 * and retry decisions. Works alongside {@code parseErrorMessage} which stores
 * the detailed technical error for debugging.</p>
 *
 * <p>Each value maps to a specific class of failure:</p>
 * <ul>
 *     <li>{@link #NETWORK} — transient; retry is likely to succeed</li>
 *     <li>{@link #CORRUPTED_FILE} — permanent; user must re-upload</li>
 *     <li>{@link #ENCRYPTED} — permanent; user must upload an unencrypted version</li>
 *     <li>{@link #EMPTY_TEXT} — permanent for text extraction; future OCR could resolve</li>
 *     <li>{@link #UNKNOWN} — catch-all for unclassified failures</li>
 * </ul>
 *
 * @see ParseStatus
 */
public enum ParseFailureReason {

    /**
     * PDF download from cloud storage failed due to a network issue.
     *
     * <p>Examples: connection timeout, HTTP 5xx, DNS resolution failure.
     * This is a transient error — retry is likely to succeed.</p>
     */
    NETWORK,

    /**
     * The PDF file is corrupted or has an invalid structure.
     *
     * <p>Examples: invalid PDF header, truncated file, malformed internal structure.
     * This is a permanent error — the user must upload a valid PDF.</p>
     */
    CORRUPTED_FILE,

    /**
     * The PDF file is password-protected or encrypted.
     *
     * <p>PDFBox cannot extract text from encrypted documents without the password.
     * The user must upload an unencrypted version of their resume.</p>
     */
    ENCRYPTED,

    /**
     * PDFBox processed the PDF successfully but extracted no text content.
     *
     * <p>This typically occurs with scanned or image-only PDFs where the content
     * is embedded as images rather than selectable text. A future OCR phase
     * could resolve this limitation.</p>
     */
    EMPTY_TEXT,

    /**
     * An unexpected or unclassified error occurred during parsing.
     *
     * <p>Catch-all for failures that do not fit the other categories.
     * The {@code parseErrorMessage} field will contain the detailed exception
     * information for investigation.</p>
     */
    UNKNOWN
}
