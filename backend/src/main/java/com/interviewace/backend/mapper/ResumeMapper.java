package com.interviewace.backend.mapper;

import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.resume.ResumeVersion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between Resume domain entities and their corresponding DTOs.
 *
 * <p>Centralizes all Resume mapping logic in one place, keeping DTOs as
 * pure data holders. This mapper handles conversions for both the
 * {@link Resume} aggregate root and {@link ResumeVersion} entities.</p>
 *
 * <p>Follows the Mapper Pattern — no business logic resides here,
 * only structural transformation between layers.</p>
 */
@Component
public class ResumeMapper {

    /**
     * Converts a Resume entity to a ResumeResponse DTO.
     *
     * <p>Maps the aggregate root fields including {@code currentVersion}
     * and {@code totalVersions} when available.</p>
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

    /**
     * Converts a {@link ResumeVersion} entity to a {@link ResumeVersionResponse} DTO.
     *
     * <p>Maps all version metadata fields. Enum values are converted to
     * their string names for JSON serialization stability.</p>
     *
     * @param version the ResumeVersion entity to convert
     * @return a fully populated ResumeVersionResponse
     */
    public ResumeVersionResponse toVersionResponse(ResumeVersion version) {
        ResumeVersionResponse response = new ResumeVersionResponse();

        response.setId(version.getId());
        response.setVersionNumber(version.getVersionNumber());
        response.setOriginalFilename(version.getOriginalFilename());
        response.setStorageProvider(version.getStorageProvider().name());
        response.setFileSize(version.getFileSize());
        response.setMimeType(version.getMimeType());
        response.setUploadedAt(version.getUploadedAt());
        response.setParseStatus(version.getParseStatus().name());
        response.setCreatedAt(version.getCreatedAt());
        response.setUpdatedAt(version.getUpdatedAt());

        return response;
    }

    /**
     * Converts a list of {@link ResumeVersion} entities to a list of
     * {@link ResumeVersionResponse} DTOs.
     *
     * @param versions the list of ResumeVersion entities
     * @return a list of fully populated ResumeVersionResponse DTOs
     */
    public List<ResumeVersionResponse> toVersionResponseList(List<ResumeVersion> versions) {
        return versions.stream()
                .map(this::toVersionResponse)
                .toList();
    }

}
