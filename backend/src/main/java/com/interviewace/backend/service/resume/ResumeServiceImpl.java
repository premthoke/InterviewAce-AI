package com.interviewace.backend.service.resume;

import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.exception.ResumeNotFoundException;
import com.interviewace.backend.mapper.ResumeMapper;
import com.interviewace.backend.repository.resume.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ResumeService}.
 *
 * <p>Manages the lifecycle of the Resume aggregate root.
 * Phase 5.1 supports read and delete only — the Resume is created lazily
 * on first upload (Phase 5.3).</p>
 *
 * <p>Depends only on {@link ResumeRepository} and {@link ResumeMapper} —
 * no Spring Security imports, no {@code SecurityContextHolder} access.</p>
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             ResumeMapper resumeMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeMapper = resumeMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResume(User user) {

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Resume not found for user: email={}", user.getEmail());
                    return new ResumeNotFoundException(
                            "No resume found. Please upload your resume first."
                    );
                });

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

}
