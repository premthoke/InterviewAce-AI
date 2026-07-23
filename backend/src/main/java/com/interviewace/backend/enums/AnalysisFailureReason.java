package com.interviewace.backend.enums;

/**
 * Enumeration representing the structured reason why a Gemini resume analysis failed.
 *
 * <p>Provides a machine-readable failure classification for dashboards, analytics,
 * and retry decisions. Works alongside {@code errorMessage} on the
 * {@code ResumeAnalysis} entity, which stores the detailed technical error
 * for debugging.</p>
 *
 * <p>Failure categories:</p>
 * <ul>
 *     <li><b>Parse failures</b> ({@link #INVALID_JSON}, {@link #SCHEMA_VIOLATION},
 *         {@link #EMPTY_RESPONSE}) — permanent; the Gemini response was unusable</li>
 *     <li><b>Network failures</b> ({@link #TIMEOUT}, {@link #RATE_LIMITED},
 *         {@link #SERVICE_UNAVAILABLE}, {@link #NETWORK_ERROR}) — transient;
 *         retry is likely to succeed</li>
 *     <li><b>Input failures</b> ({@link #TOKEN_OVERFLOW}) — permanent;
 *         the resume text exceeds model capacity despite truncation</li>
 *     <li><b>Catch-all</b> ({@link #UNKNOWN}) — unclassified errors</li>
 * </ul>
 *
 * @see AnalysisStatus
 */
public enum AnalysisFailureReason {

    // ─── Parse Failures (Permanent) ──────────────────────────────────

    /**
     * Gemini returned text that is not valid JSON.
     *
     * <p>The raw response is preserved in {@code rawResponse} for debugging.
     * This typically indicates a prompt engineering issue where the model
     * returned free-form text instead of structured JSON.</p>
     */
    INVALID_JSON,

    /**
     * The JSON is syntactically valid but violates the expected schema.
     *
     * <p>Examples: missing required fields ({@code summary}, {@code skills}),
     * wrong types ({@code atsScore} as a string instead of integer),
     * out-of-range values ({@code confidence > 1.0}).</p>
     */
    SCHEMA_VIOLATION,

    /**
     * Gemini returned an empty or whitespace-only response.
     *
     * <p>Rare but possible when the model refuses to answer or encounters
     * an internal content filtering issue.</p>
     */
    EMPTY_RESPONSE,

    // ─── Network Failures (Transient — Retryable) ────────────────────

    /**
     * The Gemini API did not respond within the configured timeout.
     *
     * <p>Default timeout is 30 seconds. Retries use exponential backoff
     * (1s, 2s). This is a transient error — retry is likely to succeed.</p>
     */
    TIMEOUT,

    /**
     * Gemini returned HTTP 429 — quota exceeded or rate limited.
     *
     * <p>Retries use longer exponential backoff (2s, 4s) to respect
     * the rate limiter. The user can also retry later via the
     * reanalyze endpoint.</p>
     */
    RATE_LIMITED,

    /**
     * Gemini returned HTTP 500, 502, or 503 — service unavailable.
     *
     * <p>Transient server-side error. Retries use exponential backoff.
     * If all retries are exhausted, the failure is persisted and the
     * user can retry later.</p>
     */
    SERVICE_UNAVAILABLE,

    /**
     * A low-level network error occurred during the Gemini API call.
     *
     * <p>Examples: DNS resolution failure, connection reset, TLS handshake
     * failure. Transient — retry is likely to succeed.</p>
     */
    NETWORK_ERROR,

    // ─── Input Failures (Permanent) ──────────────────────────────────

    /**
     * The resume text exceeds the model's context window despite truncation.
     *
     * <p>The {@code PromptBuilder} truncates long resumes before sending,
     * but if the truncated text still exceeds the limit (e.g., due to a
     * very large system prompt), this error is raised.</p>
     */
    TOKEN_OVERFLOW,

    // ─── Catch-All ───────────────────────────────────────────────────

    /**
     * An unexpected or unclassified error occurred during analysis.
     *
     * <p>Catch-all for failures that do not fit the other categories.
     * The {@code errorMessage} field on the {@code ResumeAnalysis} entity
     * will contain the detailed exception information for investigation.</p>
     */
    UNKNOWN
}
