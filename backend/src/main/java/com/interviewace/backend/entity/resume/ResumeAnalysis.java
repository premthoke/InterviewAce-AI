package com.interviewace.backend.entity.resume;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.enums.AnalysisFailureReason;
import com.interviewace.backend.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain entity representing the result of a Gemini AI analysis on a resume version.
 *
 * <p>This is a <b>separate aggregate</b> from {@link ResumeVersion} by design.
 * While a resume version represents <i>what was uploaded</i>, this entity captures
 * <i>what the AI concluded</i>. Keeping them separate enables:</p>
 *
 * <ul>
 *     <li><b>Multiple analyses per version</b> — re-analysis with new prompts or
 *         model versions creates a new entity, preserving history</li>
 *     <li><b>Model versioning</b> — side-by-side comparison across model generations</li>
 *     <li><b>Independent scaling</b> — the AI pipeline can scale without touching
 *         the upload pathway</li>
 *     <li><b>Clean lifecycle separation</b> — deleting an analysis never affects
 *         the resume; deleting a version cascades to its analyses</li>
 * </ul>
 *
 * <p>JSON array fields ({@code skills}, {@code strengths}, {@code weaknesses},
 * {@code suggestions}) are stored as TEXT columns containing JSON strings.
 * This is intentional for Phase 5.5B — a future phase will introduce a
 * JPA {@code AttributeConverter<List<String>, String>} for type-safe access.
 * Business logic should <b>not</b> parse these as comma-separated strings;
 * they are always valid JSON arrays.</p>
 *
 * <p>The {@code rawResponse} field stores the complete, unprocessed Gemini output.
 * This field is <b>internal only</b> — it must <b>never</b> be exposed through
 * REST API responses. It may contain prompt details, model metadata, and
 * internal formatting that should remain confidential.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *     <li>{@code resumeVersion} — owning side of the many-to-one with
 *         {@link ResumeVersion}. Multiple analyses can exist per version.</li>
 * </ul>
 *
 * @see ResumeVersion
 * @see AnalysisStatus
 * @see AnalysisFailureReason
 */
@Entity
@Table(name = "resume_analyses", indexes = {
        @Index(name = "idx_ra_version_id", columnList = "resume_version_id"),
        @Index(name = "idx_ra_version_status", columnList = "resume_version_id, analysis_status"),
        @Index(name = "idx_ra_model_name", columnList = "model_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis extends BaseEntity {

    /* ------------------------------------------------------------------ */
    /*  Relationships                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * The specific resume version that was analyzed.
     *
     * <p>Many-to-one: multiple analyses can exist per version (re-analysis,
     * model upgrades, prompt A/B testing). The inverse side is
     * {@link ResumeVersion#getAnalyses()}.</p>
     *
     * <p>Cascade is intentionally omitted — analysis lifecycle is managed
     * by the service layer. Deletion cascades from version to analyses
     * via {@code ON DELETE CASCADE} at the database level.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_version_id", nullable = false)
    private ResumeVersion resumeVersion;

    /* ------------------------------------------------------------------ */
    /*  Status & Tracking                                                  */
    /* ------------------------------------------------------------------ */

    /**
     * Current status in the analysis lifecycle.
     *
     * <p>Defaults to {@link AnalysisStatus#NOT_STARTED} on entity creation.
     * The orchestrator transitions this through the state machine:
     * NOT_STARTED → IN_PROGRESS → COMPLETED | FAILED.</p>
     *
     * @see AnalysisStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 20)
    @Builder.Default
    private AnalysisStatus analysisStatus = AnalysisStatus.NOT_STARTED;

    /**
     * The Gemini model identifier used for this analysis.
     *
     * <p>Example: {@code gemini-2.5-flash}. Enables filtering by model
     * and comparing outputs across model generations.</p>
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /**
     * Specific model version or snapshot identifier if provided by the API.
     *
     * <p>Optional because Gemini may not always expose granular version strings.
     * Separated from {@code modelName} to allow querying "all analyses by model X"
     * without parsing compound strings.</p>
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    /* ------------------------------------------------------------------ */
    /*  Analysis Results                                                   */
    /* ------------------------------------------------------------------ */

    /**
     * AI-generated 2–4 sentence summary of the resume.
     *
     * <p>The "elevator pitch" for the candidate. {@code null} until
     * analysis completes successfully.</p>
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * JSON array of identified skills.
     *
     * <p>Stored as a JSON string (e.g., {@code ["Java", "Spring Boot", "AWS"]}).
     * Designed for future migration to a JPA {@code AttributeConverter<List<String>, String>}
     * — do not treat as comma-separated or plain text.</p>
     *
     * <p>{@code null} until analysis completes successfully.</p>
     */
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    /**
     * JSON array of identified strengths.
     *
     * <p>Stored as a JSON string. Each item is a 1–2 sentence description.
     * Designed for future {@code AttributeConverter} migration.</p>
     *
     * <p>{@code null} until analysis completes successfully.</p>
     */
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    /**
     * JSON array of identified weaknesses or areas for improvement.
     *
     * <p>Stored as a JSON string. Each item is a 1–2 sentence description.
     * Designed for future {@code AttributeConverter} migration.</p>
     *
     * <p>{@code null} until analysis completes successfully.</p>
     */
    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    /**
     * JSON array of actionable improvement suggestions.
     *
     * <p>Stored as a JSON string. Each item is a 1–2 sentence actionable suggestion.
     * Designed for future {@code AttributeConverter} migration.</p>
     *
     * <p>{@code null} until analysis completes successfully.</p>
     */
    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    /**
     * AI-estimated ATS (Applicant Tracking System) compatibility score.
     *
     * <p>Range: 0–100. Represents how well the resume would perform
     * against automated screening. {@code null} until analysis completes.</p>
     */
    @Column(name = "ats_score")
    private Integer atsScore;

    /**
     * Model's self-reported confidence in the analysis.
     *
     * <p>Range: 0.0–1.0 (probability, not percentage). Stored as DOUBLE
     * to preserve precision for downstream statistical analysis. Frontend
     * can display as percentage via {@code Math.round(confidence * 100)}.</p>
     *
     * <p>{@code null} until analysis completes.</p>
     */
    @Column(name = "confidence")
    private Double confidence;

    /* ------------------------------------------------------------------ */
    /*  Prompt & Model Metadata                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Semantic version string of the prompt template used.
     *
     * <p>Example: {@code 1.0.0}. Critical for A/B testing prompts and
     * diagnosing regressions when prompts change. Mandatory — every
     * analysis must be traceable to the exact prompt that produced it.</p>
     */
    @Column(name = "prompt_version", nullable = false, length = 20)
    private String promptVersion;

    /**
     * The complete, unprocessed JSON string returned by Gemini.
     *
     * <p><b>INTERNAL ONLY — must NEVER be exposed through REST API responses.</b></p>
     *
     * <p>Stored for debugging, auditing, and prompt engineering iteration.
     * May contain prompt details, model metadata, and internal formatting
     * that should remain confidential. Enables offline re-parsing when
     * the {@code ResponseParser} logic changes.</p>
     *
     * <p>Preserved on <b>every</b> outcome (success and failure) — engineers
     * need to see exactly what Gemini returned, even if it was malformed.</p>
     */
    @Lob
    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse;

    /* ------------------------------------------------------------------ */
    /*  Timing                                                             */
    /* ------------------------------------------------------------------ */

    /**
     * Timestamp when the Gemini API call was initiated.
     *
     * <p>Used for duration computation, timeout detection, and
     * performance monitoring.</p>
     */
    @Column(name = "analysis_started_at")
    private LocalDateTime analysisStartedAt;

    /**
     * Timestamp when the analysis finished (success or failure).
     *
     * <p>Set on both {@code COMPLETED} and {@code FAILED} transitions.</p>
     */
    @Column(name = "analysis_completed_at")
    private LocalDateTime analysisCompletedAt;

    /* ------------------------------------------------------------------ */
    /*  Failure Information                                                 */
    /* ------------------------------------------------------------------ */

    /**
     * Structured failure category when the analysis fails.
     *
     * <p>Only set when {@code analysisStatus == FAILED}; {@code null} otherwise.
     * Provides a machine-readable classification for dashboards, analytics,
     * and automated retry decisions.</p>
     *
     * @see AnalysisFailureReason
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 30)
    private AnalysisFailureReason failureReason;

    /**
     * Detailed error message describing why analysis failed.
     *
     * <p>Stores the technical exception message for debugging.
     * Truncated to 1000 characters to match the existing pattern
     * in {@link ResumeVersion#getParseErrorMessage()}.</p>
     *
     * <p><b>Internal only</b> — should not be exposed in API responses.</p>
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /* ------------------------------------------------------------------ */
    /*  Token Usage                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Total tokens consumed by this analysis (prompt + completion).
     *
     * <p>Named {@code totalTokens} (not {@code tokenCount}) for future
     * extensibility — when Gemini usage metadata is captured, this field
     * can be complemented by {@code promptTokens} and {@code completionTokens}
     * columns without renaming.</p>
     *
     * <p>Used for cost tracking and quota monitoring.</p>
     */
    @Column(name = "total_tokens")
    private Integer totalTokens;

    /* ------------------------------------------------------------------ */
    /*  Derived Helpers (Transient — Not Persisted)                         */
    /* ------------------------------------------------------------------ */

    /**
     * Computes the analysis duration in milliseconds.
     *
     * <p>Derived from {@code analysisStartedAt} and {@code analysisCompletedAt}.
     * Returns {@code null} if either timestamp is not set (analysis not yet
     * started or not yet completed).</p>
     *
     * <p>This is a convenience method for monitoring and DTO mapping —
     * no database column is needed.</p>
     *
     * @return duration in milliseconds, or {@code null} if timestamps are incomplete
     */
    public Long getAnalysisDurationMs() {
        if (analysisStartedAt == null || analysisCompletedAt == null) {
            return null;
        }
        return Duration.between(analysisStartedAt, analysisCompletedAt).toMillis();
    }
}
