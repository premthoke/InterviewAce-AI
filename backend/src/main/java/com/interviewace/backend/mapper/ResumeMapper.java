package com.interviewace.backend.mapper;

import com.interviewace.backend.dto.resume.ParseStatusResponse;
import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.resume.ResumeVersion;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
     * <p>Maps all version metadata fields including parsing status and timing.
     * Enum values are converted to their string names for JSON serialization
     * stability. The {@code hasText} field is derived from the entity's
     * {@code parsedText} — the raw text itself is never exposed in the API.</p>
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

        // Phase 5.4B — Parsing fields
        response.setParseStartedAt(version.getParseStartedAt());
        response.setParseCompletedAt(version.getParseCompletedAt());
        response.setHasText(version.getParsedText() != null && !version.getParsedText().isBlank());
        response.setWordCount(version.getWordCount());

        return response;
    }

    /**
     * Converts a {@link ResumeVersion} entity to a {@link ParseStatusResponse} DTO.
     *
     * <p>Purpose-built for the {@code GET /api/resume/versions/{id}/parse-status}
     * endpoint. Includes computed fields:</p>
     * <ul>
     *     <li>{@code parseDurationMs} — derived from the difference between
     *         {@code parseStartedAt} and {@code parseCompletedAt}</li>
     *     <li>{@code hasText} — derived from {@code parsedText != null && !parsedText.isBlank()}</li>
     *     <li>{@code textLength} — derived from {@code parsedText.length()}</li>
     * </ul>
     *
     * @param version the ResumeVersion entity to convert
     * @return a fully populated ParseStatusResponse
     */
    public ParseStatusResponse toParseStatusResponse(ResumeVersion version) {
        ParseStatusResponse response = new ParseStatusResponse();

        response.setVersionId(version.getId());
        response.setParseStatus(version.getParseStatus().name());
        response.setParseStartedAt(version.getParseStartedAt());
        response.setParseCompletedAt(version.getParseCompletedAt());

        // Computed: parse duration in milliseconds
        if (version.getParseStartedAt() != null && version.getParseCompletedAt() != null) {
            response.setParseDurationMs(
                    Duration.between(version.getParseStartedAt(), version.getParseCompletedAt()).toMillis()
            );
        }

        // Computed: text presence indicator (raw text is never exposed)
        boolean hasText = version.getParsedText() != null && !version.getParsedText().isBlank();
        response.setHasText(hasText);

        // Computed: text length (character count)
        if (hasText) {
            response.setTextLength(version.getParsedText().length());
        }

        response.setWordCount(version.getWordCount());

        // Failure details (only present when parseStatus = FAILED)
        if (version.getParseFailureReason() != null) {
            response.setParseFailureReason(version.getParseFailureReason().name());
        }
        response.setParseErrorMessage(version.getParseErrorMessage());

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
