package com.interviewace.backend.entity.resume;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.enums.ParseFailureReason;
import com.interviewace.backend.enums.ParseStatus;
import com.interviewace.backend.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Domain entity representing a single version of a user's resume.
 *
 * <p>Each {@link Resume} aggregate can have unlimited versions. A version
 * captures the metadata of an uploaded resume file, including its storage
 * location, file characteristics, and parsing status.</p>
 *
 * <p>Version numbering is monotonically increasing per resume. The latest
 * version is tracked via {@link Resume#getCurrentVersion()}.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *     <li>{@code resume} — owning side of the many-to-one relationship
 *         with {@link Resume}.</li>
 *     <li>{@code analyses} — inverse side of the one-to-many relationship
 *         with {@link ResumeAnalysis}. A version can have multiple AI analyses
 *         (re-analysis, model upgrades, prompt A/B testing).</li>
 * </ul>
 *
 * @see Resume
 * @see StorageProvider
 * @see ParseStatus
 * @see ParseFailureReason
 */
@Entity
@Table(name = "resume_versions", indexes = {
        @Index(name = "idx_rv_resume_id", columnList = "resume_id"),
        @Index(name = "idx_rv_resume_version", columnList = "resume_id, version_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeVersion extends BaseEntity {

    /* ------------------------------------------------------------------ */
    /*  Relationships                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * The resume aggregate that owns this version.
     * Owning side — the foreign key column resides in the {@code resume_versions} table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    /**
     * All AI analyses performed on this resume version.
     *
     * <p>Inverse (non-owning) side of the bidirectional relationship.
     * A single version can have multiple analyses — each re-analysis
     * or model upgrade creates a new {@link ResumeAnalysis} entity
     * rather than overwriting the previous one.</p>
     *
     * <p>No cascade is configured — analysis lifecycle is managed
     * by the service layer. Database-level {@code ON DELETE CASCADE}
     * on the FK ensures cleanup when a version is deleted.</p>
     */
    @OneToMany(mappedBy = "resumeVersion", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResumeAnalysis> analyses = new ArrayList<>();

    /* ------------------------------------------------------------------ */
    /*  Version Metadata                                                   */
    /* ------------------------------------------------------------------ */

    /**
     * Monotonically increasing version number within the parent resume.
     * Starts at 1 and increments by 1 for each new upload.
     */
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    /**
     * The original filename as provided by the user during upload.
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * The storage backend where the resume file is persisted.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false)
    private StorageProvider storageProvider;

    /**
     * The size of the uploaded file in bytes.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * The MIME type of the uploaded file (e.g., {@code application/pdf}).
     */
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    /**
     * The timestamp when the file was uploaded.
     * Distinct from {@code createdAt} which tracks entity creation.
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * The current parsing status of this resume version.
     * Defaults to {@link ParseStatus#NOT_STARTED}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false)
    @Builder.Default
    private ParseStatus parseStatus = ParseStatus.NOT_STARTED;

    /* ------------------------------------------------------------------ */
    /*  Storage Fields (Phase 5.3)                                         */
    /* ------------------------------------------------------------------ */

    /**
     * The full HTTPS URL to retrieve the file from the storage provider.
     *
     * <p>Stored denormalized for fast reads — avoids reconstructing
     * URLs from provider-specific components at read time.</p>
     */
    @Column(name = "storage_url", nullable = false, length = 500)
    private String storageUrl;

    /**
     * The storage provider's unique resource identifier.
     *
     * <p>Required for delete, update, and admin operations against
     * the storage backend (e.g., Cloudinary public ID). This is an
     * internal field — never exposed in API responses.</p>
     */
    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    /**
     * SHA-256 hash of the uploaded file bytes.
     *
     * <p>Computed during upload for:</p>
     * <ul>
     *     <li>Detecting identical re-uploads (user feedback)</li>
     *     <li>Integrity verification on download</li>
     *     <li>Future deduplication optimization</li>
     * </ul>
     *
     * <p>Stored as a 64-character lowercase hex string.</p>
     */
    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    /* ------------------------------------------------------------------ */
    /*  Parsing Fields (Phase 5.4)                                         */
    /* ------------------------------------------------------------------ */

    /**
     * The full text content extracted from the PDF resume.
     *
     * <p>Populated by the PDF parsing pipeline after successful text extraction.
     * Stored as {@code LONGTEXT} (up to ~4GB) to accommodate any resume length.
     * {@code null} until parsing completes successfully.</p>
     *
     * <p>This field is the primary input for the future Gemini AI analysis pipeline.
     * It is intentionally <b>not</b> exposed in API responses — only the AI layer
     * accesses it directly.</p>
     */
    @Lob
    @Column(name = "parsed_text", columnDefinition = "LONGTEXT")
    private String parsedText;

    /**
     * The timestamp when the parsing process began.
     *
     * <p>Used for:</p>
     * <ul>
     *     <li>Timeout detection (PENDING entries older than threshold)</li>
     *     <li>Parse duration computation ({@code parseCompletedAt - parseStartedAt})</li>
     *     <li>Performance monitoring and analytics</li>
     * </ul>
     */
    @Column(name = "parse_started_at")
    private LocalDateTime parseStartedAt;

    /**
     * The timestamp when the parsing process finished (success or failure).
     *
     * <p>Set on both {@code COMPLETED} and {@code FAILED} transitions.
     * Combined with {@code parseStartedAt} to compute parse duration.</p>
     */
    @Column(name = "parse_completed_at")
    private LocalDateTime parseCompletedAt;

    /**
     * The structured reason category for a parsing failure.
     *
     * <p>Provides a machine-readable failure classification for dashboards,
     * analytics, and automated retry decisions. Only set when
     * {@code parseStatus = FAILED}; {@code null} otherwise.</p>
     *
     * @see ParseFailureReason
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "parse_failure_reason")
    private ParseFailureReason parseFailureReason;

    /**
     * Detailed error message describing why parsing failed.
     *
     * <p>Stores the technical exception message for debugging purposes.
     * Works alongside {@code parseFailureReason} which provides the
     * structured category. Only set when {@code parseStatus = FAILED}.</p>
     */
    @Column(name = "parse_error_message", length = 1000)
    private String parseErrorMessage;

    /**
     * The number of words in the extracted text.
     *
     * <p>Computed during parsing by splitting on whitespace boundaries.
     * Used for:</p>
     * <ul>
     *     <li>Resume statistics and analytics</li>
     *     <li>ATS score normalization</li>
     *     <li>Gemini prompt token budgeting</li>
     *     <li>Interview question generation calibration</li>
     * </ul>
     *
     * <p>{@code null} until parsing completes successfully.</p>
     */
    @Column(name = "word_count")
    private Integer wordCount;
}

