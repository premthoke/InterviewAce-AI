package com.interviewace.backend.entity.resume;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for the Resume domain.
 *
 * <p>Each user owns at most one Resume. The Resume acts as a stable anchor
 * that future modules (Interview, Job Matching) reference. Individual uploads
 * are stored as {@link ResumeVersion} entities.</p>
 *
 * <p>This entity is created lazily — only when the user uploads their first
 * resume. There are never empty Resume rows in the database.</p>
 *
 * <p>Relationship to User is unidirectional (Resume → User), following the
 * same pattern as Profile → User. The User entity stays focused solely on
 * authentication.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *     <li>{@code user} — owning side of the one-to-one with {@link User}</li>
 *     <li>{@code versions} — one-to-many collection of all {@link ResumeVersion} uploads</li>
 *     <li>{@code currentVersion} — quick-access pointer to the latest active version</li>
 * </ul>
 *
 * <p>No cascade is configured on versions. Deletion of related entities
 * (ResumeVersion, ResumeAnalysis, Cloudinary files) will be handled explicitly
 * by a dedicated service in the application layer, not silently by the database.</p>
 *
 * @see ResumeVersion
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "resumes")
public class Resume extends BaseEntity {

    /* ------------------------------------------------------------------ */
    /*  User Relationship                                                  */
    /* ------------------------------------------------------------------ */

    /**
     * The user who owns this resume aggregate.
     * Owning side — the foreign key column resides in the {@code resumes} table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /* ------------------------------------------------------------------ */
    /*  Version Relationships                                              */
    /* ------------------------------------------------------------------ */

    /**
     * All versions of this resume, ordered by version number descending.
     *
     * <p>Supports unlimited versioning — every upload creates a new
     * {@link ResumeVersion} appended to this collection.</p>
     */
    @OneToMany(mappedBy = "resume", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResumeVersion> versions = new ArrayList<>();

    /**
     * Pointer to the current (latest) version of this resume.
     *
     * <p>This provides O(1) access to the active version without
     * requiring a query over the {@code versions} collection.
     * Updated atomically each time a new version is uploaded.</p>
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private ResumeVersion currentVersion;

}
