package com.interviewace.backend.repository.resume;

import com.interviewace.backend.entity.resume.ResumeAnalysis;
import com.interviewace.backend.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ResumeAnalysis} entities.
 *
 * <p>Provides standard CRUD operations plus custom finders for
 * querying analyses within a resume version's scope.</p>
 *
 * <p>Key query patterns:</p>
 * <ul>
 *     <li><b>Latest analysis</b> — {@link #findTopByResumeVersionIdOrderByIdDesc(Long)}
 *         returns the most recent analysis (highest ID) for a version</li>
 *     <li><b>All analyses</b> — {@link #findAllByResumeVersionId(Long)}
 *         returns the full analysis history for a version</li>
 *     <li><b>Status check</b> — {@link #existsByResumeVersionIdAndAnalysisStatus(Long, AnalysisStatus)}
 *         checks if an analysis with a specific status exists (e.g., IN_PROGRESS guard)</li>
 *     <li><b>Status query</b> — {@link #findByResumeVersionIdAndAnalysisStatus(Long, AnalysisStatus)}
 *         retrieves analyses filtered by both version and status</li>
 * </ul>
 *
 * <p>All queries filter by {@code resumeVersionId} to ensure analyses
 * are always scoped to a specific version. Cross-version queries
 * (e.g., admin analytics by model name) can be added as needed.</p>
 *
 * @see ResumeAnalysis
 * @see AnalysisStatus
 */
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    /**
     * Finds the most recent analysis for the given resume version.
     *
     * <p>Returns the analysis with the highest {@code id} (most recently created).
     * This is the primary query for "get analysis" endpoints — users see
     * the latest analysis result.</p>
     *
     * @param resumeVersionId the resume version ID to query
     * @return the most recent analysis, or empty if none exist
     */
    Optional<ResumeAnalysis> findTopByResumeVersionIdOrderByIdDesc(Long resumeVersionId);

    /**
     * Finds all analyses for the given resume version.
     *
     * <p>Returns the full analysis history, including re-analyses and
     * failed attempts. Useful for comparison views and audit trails.</p>
     *
     * @param resumeVersionId the resume version ID to query
     * @return list of all analyses for the version (may be empty)
     */
    List<ResumeAnalysis> findAllByResumeVersionId(Long resumeVersionId);

    /**
     * Checks if an analysis with the given status exists for the version.
     *
     * <p>Primary use case: guard against duplicate Gemini API calls by
     * checking {@code existsByResumeVersionIdAndAnalysisStatus(id, IN_PROGRESS)}
     * before starting a new analysis.</p>
     *
     * @param resumeVersionId the resume version ID to check
     * @param analysisStatus  the status to check for
     * @return {@code true} if at least one matching analysis exists
     */
    boolean existsByResumeVersionIdAndAnalysisStatus(Long resumeVersionId, AnalysisStatus analysisStatus);

    /**
     * Finds all analyses for the given version that have the specified status.
     *
     * <p>Useful for querying in-progress analyses before launching a new one,
     * or for finding all failed analyses for a retry dashboard.</p>
     *
     * @param resumeVersionId the resume version ID to query
     * @param analysisStatus  the status to filter by
     * @return list of matching analyses (may be empty)
     */
    List<ResumeAnalysis> findByResumeVersionIdAndAnalysisStatus(Long resumeVersionId, AnalysisStatus analysisStatus);
}
