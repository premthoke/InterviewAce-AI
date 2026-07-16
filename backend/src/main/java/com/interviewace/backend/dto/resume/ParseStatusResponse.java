package com.interviewace.backend.dto.resume;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for the parse status endpoint.
 *
 * <p>Provides detailed parsing metadata including timing information,
 * content indicators, and failure details. This DTO is purpose-built
 * for the {@code GET /api/resume/versions/{id}/parse-status} endpoint.</p>
 *
 * <p>Includes computed fields like {@code parseDurationMs} and
 * {@code textLength} that are derived from entity data during mapping.</p>
 *
 * @see com.interviewace.backend.entity.resume.ResumeVersion
 * @see com.interviewace.backend.mapper.ResumeMapper#toParseStatusResponse
 */
@Getter
@Setter
@NoArgsConstructor
public class ParseStatusResponse {

    /**
     * The ID of the resume version.
     */
    private Long versionId;

    /**
     * The current parse status (e.g., NOT_STARTED, PENDING, COMPLETED, FAILED).
     */
    private String parseStatus;

    /**
     * The timestamp when the parsing process began.
     */
    private LocalDateTime parseStartedAt;

    /**
     * The timestamp when the parsing process finished (success or failure).
     */
    private LocalDateTime parseCompletedAt;

    /**
     * The duration of the parse operation in milliseconds.
     *
     * <p>Computed as {@code Duration.between(parseStartedAt, parseCompletedAt).toMillis()}.
     * {@code null} if either timestamp is not set.</p>
     */
    private Long parseDurationMs;

    /**
     * Whether the version has extracted text content.
     */
    private Boolean hasText;

    /**
     * The character count of the extracted text.
     *
     * <p>{@code null} if no text has been extracted.</p>
     */
    private Integer textLength;

    /**
     * The word count of the extracted text.
     *
     * <p>{@code null} if no text has been extracted.</p>
     */
    private Integer wordCount;

    /**
     * The structured failure reason category.
     *
     * <p>{@code null} if parsing has not failed.</p>
     */
    private String parseFailureReason;

    /**
     * The detailed error message describing the failure.
     *
     * <p>{@code null} if parsing has not failed.</p>
     */
    private String parseErrorMessage;
}
