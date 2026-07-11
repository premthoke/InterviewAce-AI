package com.interviewace.backend.entity.resume;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.enums.ParseStatus;
import com.interviewace.backend.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.*;

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
 * </ul>
 *
 * @see Resume
 * @see StorageProvider
 * @see ParseStatus
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
}
