package com.interviewace.backend.repository.resume;

import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.resume.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ResumeVersion} entities.
 *
 * <p>Provides standard CRUD operations plus custom finders
 * for querying resume versions within a resume aggregate.</p>
 *
 * <p>All query methods accept a {@link Resume} parameter to ensure
 * version queries are always scoped to a specific resume aggregate,
 * preventing cross-user data leakage.</p>
 */
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    /**
     * Finds all versions belonging to the given resume.
     *
     * @param resume the resume aggregate to query
     * @return list of all versions for the resume
     */
    List<ResumeVersion> findByResume(Resume resume);

    /**
     * Finds the latest version for the given resume (highest version number).
     *
     * <p>Used to determine the next version number during upload
     * and to resolve the current version when {@code currentVersion}
     * is not yet set.</p>
     *
     * @param resume the resume aggregate to query
     * @return the latest version if any exist
     */
    Optional<ResumeVersion> findTopByResumeOrderByVersionNumberDesc(Resume resume);

    /**
     * Finds all versions for the given resume, ordered by version number descending.
     *
     * <p>Returns the most recent version first, which is the natural
     * display order for a version history list.</p>
     *
     * @param resume the resume aggregate to query
     * @return list of versions ordered newest-first
     */
    List<ResumeVersion> findByResumeOrderByVersionNumberDesc(Resume resume);

    /**
     * Counts the total number of versions for the given resume.
     *
     * <p>Useful for pagination metadata and for determining the next
     * version number without loading all version entities.</p>
     *
     * @param resume the resume aggregate to query
     * @return the total number of versions
     */
    long countByResume(Resume resume);

}
