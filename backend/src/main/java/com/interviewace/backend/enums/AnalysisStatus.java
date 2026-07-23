package com.interviewace.backend.enums;

/**
 * Enumeration representing the lifecycle status of a Gemini resume analysis.
 *
 * <p>Tracks the progress of AI-powered resume analysis through
 * a well-defined state machine:</p>
 *
 * <pre>
 *   [*] → NOT_STARTED → IN_PROGRESS → COMPLETED
 *                                   ↘ FAILED
 *   FAILED     → IN_PROGRESS  (retry via reanalyze)
 *   COMPLETED  → IN_PROGRESS  (re-run via reanalyze — creates new entity)
 * </pre>
 *
 * <p>Key design decisions:</p>
 * <ul>
 *     <li>{@link #NOT_STARTED} is a brief transient state — typically
 *         transitions immediately to {@link #IN_PROGRESS}</li>
 *     <li>Re-analysis creates a <b>new</b> {@code ResumeAnalysis} entity
 *         rather than mutating the existing one, preserving full history</li>
 *     <li>An {@code IN_PROGRESS} analysis blocks new analysis requests
 *         (409 Conflict) to prevent duplicate Gemini API calls</li>
 * </ul>
 *
 * @see AnalysisFailureReason
 */
public enum AnalysisStatus {

    /**
     * Analysis entity has been created but the Gemini API call
     * has not yet been initiated.
     *
     * <p>This is a brief transient state — the orchestrator transitions
     * to {@link #IN_PROGRESS} almost immediately after entity creation.</p>
     */
    NOT_STARTED,

    /**
     * The Gemini API call is active. The prompt has been sent
     * and the system is awaiting a response.
     *
     * <p>While in this state, new analysis requests for the same
     * resume version are rejected with HTTP 409 to prevent
     * duplicate API calls and wasted quota.</p>
     */
    IN_PROGRESS,

    /**
     * Analysis finished successfully. All result fields
     * ({@code summary}, {@code skills}, {@code strengths},
     * {@code weaknesses}, {@code suggestions}, {@code atsScore},
     * {@code confidence}) are populated.
     */
    COMPLETED,

    /**
     * Analysis failed. The {@code failureReason} and {@code errorMessage}
     * fields on the associated {@code ResumeAnalysis} entity contain
     * details about what went wrong.
     *
     * <p>Users can retry by calling the reanalyze endpoint, which
     * creates a new analysis entity rather than mutating this one.</p>
     *
     * @see AnalysisFailureReason
     */
    FAILED
}
