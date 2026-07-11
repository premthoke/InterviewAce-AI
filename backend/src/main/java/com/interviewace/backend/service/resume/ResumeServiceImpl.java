package com.interviewace.backend.service.resume;

import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.resume.ResumeVersion;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.exception.ResumeNotFoundException;
import com.interviewace.backend.mapper.ResumeMapper;
import com.interviewace.backend.repository.resume.ResumeRepository;
import com.interviewace.backend.repository.resume.ResumeVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ResumeService}.
 *
 * <p>Manages the lifecycle of the Resume aggregate root and its versions.
 * Phase 5.1 supports read and delete. Phase 5.2 adds version query
 * operations ({@link #getVersions}, {@link #getCurrentVersion}).</p>
 *
 * <p>Depends only on repositories and the mapper — no Spring Security
 * imports, no {@code SecurityContextHolder} access.</p>
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeMapper resumeMapper;

    /**
     * Constructor injection for all dependencies.
     *
     * @param resumeRepository        repository for Resume aggregate root
     * @param resumeVersionRepository repository for ResumeVersion entities
     * @param resumeMapper            mapper for entity-to-DTO conversions
     */
    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             ResumeVersionRepository resumeVersionRepository,
                             ResumeMapper resumeMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeMapper = resumeMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResume(User user) {

        Resume resume = findResumeOrThrow(user);
        return resumeMapper.toResponse(resume);
    }

    /**
     * {@inheritDoc}
     *
     * <p>In Phase 5.1, this deletes the Resume entity only.
     * Future phases will add cleanup for ResumeVersions, ResumeAnalyses,
     * and Cloudinary files before deleting the aggregate.</p>
     */
    @Override
    @Transactional
    public void deleteResume(User user) {

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Cannot delete: resume not found for user: email={}", user.getEmail());
                    return new ResumeNotFoundException(
                            "No resume found to delete."
                    );
                });

        // Phase 5.2+: Delete all ResumeVersions, ResumeAnalyses, and Cloudinary files here.

        resumeRepository.delete(resume);
        log.info("Resume deleted successfully for user: email={}", user.getEmail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumeVersionResponse> getVersions(User user) {

        Resume resume = findResumeOrThrow(user);

        List<ResumeVersion> versions = resumeVersionRepository
                .findByResumeOrderByVersionNumberDesc(resume);

        log.info("Retrieved {} version(s) for user: email={}",
                versions.size(), user.getEmail());

        return resumeMapper.toVersionResponseList(versions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeVersionResponse getCurrentVersion(User user) {

        Resume resume = findResumeOrThrow(user);

        // Prefer the currentVersion pointer on the aggregate root.
        // Fall back to the highest version number if the pointer is null.
        ResumeVersion currentVersion = resume.getCurrentVersion();

        if (currentVersion == null) {
            currentVersion = resumeVersionRepository
                    .findTopByResumeOrderByVersionNumberDesc(resume)
                    .orElseThrow(() -> {
                        log.warn("No versions exist for user: email={}", user.getEmail());
                        return new ResumeNotFoundException(
                                "No resume versions found. Please upload your resume first."
                        );
                    });
        }

        log.info("Current version (v{}) retrieved for user: email={}",
                currentVersion.getVersionNumber(), user.getEmail());

        return resumeMapper.toVersionResponse(currentVersion);
    }

    /* ------------------------------------------------------------------ */
    /*  Private Helpers                                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Finds the resume for the given user or throws {@link ResumeNotFoundException}.
     *
     * @param user the authenticated user
     * @return the user's Resume entity
     * @throws ResumeNotFoundException if no resume exists for the user
     */
    private Resume findResumeOrThrow(User user) {
        return resumeRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Resume not found for user: email={}", user.getEmail());
                    return new ResumeNotFoundException(
                            "No resume found. Please upload your resume first."
                    );
                });
    }

}
