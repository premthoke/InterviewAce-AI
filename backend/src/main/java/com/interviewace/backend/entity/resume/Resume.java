package com.interviewace.backend.entity.resume;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate root for the Resume domain.
 *
 * <p>Each user owns at most one Resume. The Resume acts as a stable anchor
 * that future modules (Interview, Job Matching) reference. Individual uploads
 * are stored as {@code ResumeVersion} entities (introduced in Phase 5.2).</p>
 *
 * <p>This entity is created lazily — only when the user uploads their first
 * resume. There are never empty Resume rows in the database.</p>
 *
 * <p>Relationship to User is unidirectional (Resume → User), following the
 * same pattern as Profile → User. The User entity stays focused solely on
 * authentication.</p>
 *
 * <p>No cascade is configured. Deletion of related entities (ResumeVersion,
 * ResumeAnalysis, Cloudinary files) will be handled explicitly by a dedicated
 * service in the application layer, not silently by the database.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "resumes")
public class Resume extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // current_version_id will be added in Phase 5.2 when ResumeVersion is introduced.
    // For now, the Resume aggregate root simply marks ownership.

}
