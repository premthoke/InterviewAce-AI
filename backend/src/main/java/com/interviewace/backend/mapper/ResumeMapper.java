package com.interviewace.backend.mapper;

import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.entity.resume.Resume;
import org.springframework.stereotype.Component;

/**
 * Maps between Resume entities and Resume DTOs.
 *
 * <p>Centralizes all Resume mapping logic in one place, keeping DTOs as
 * pure data holders. Future phases will add version-related mappings
 * (ResumeVersionResponse, ResumeAnalysisResponse) here.</p>
 */
@Component
public class ResumeMapper {

    /**
     * Converts a Resume entity to a ResumeResponse DTO.
     *
     * <p>In Phase 5.1 this maps only the aggregate root fields.
     * Phase 5.2 will add {@code currentVersion} and {@code totalVersions}.</p>
     *
     * @param resume the Resume entity to convert
     * @return a fully populated ResumeResponse
     */
    public ResumeResponse toResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();

        response.setId(resume.getId());
        response.setCreatedAt(resume.getCreatedAt());
        response.setUpdatedAt(resume.getUpdatedAt());

        return response;
    }

}
