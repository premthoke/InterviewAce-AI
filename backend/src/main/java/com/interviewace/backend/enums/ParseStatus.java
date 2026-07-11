package com.interviewace.backend.enums;

/**
 * Enumeration representing the parsing lifecycle status of a resume version.
 *
 * <p>Tracks the progress of resume content extraction (e.g., PDF parsing)
 * through a well-defined state machine:</p>
 *
 * <pre>
 *   NOT_STARTED → PENDING → COMPLETED
 *                        ↘ FAILED
 * </pre>
 *
 * <ul>
 *     <li>{@link #NOT_STARTED} — parsing has not yet been initiated</li>
 *     <li>{@link #PENDING} — parsing is currently in progress</li>
 *     <li>{@link #COMPLETED} — parsing finished successfully</li>
 *     <li>{@link #FAILED} — parsing encountered an error</li>
 * </ul>
 */
public enum ParseStatus {

    /**
     * Parsing has not yet been initiated for this resume version.
     */
    NOT_STARTED,

    /**
     * Parsing is currently in progress.
     */
    PENDING,

    /**
     * Parsing completed successfully and extracted content is available.
     */
    COMPLETED,

    /**
     * Parsing encountered an error and could not extract content.
     */
    FAILED
}
