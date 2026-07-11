package com.interviewace.backend.dto.resume;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single resume version.
 *
 * <p>Exposes only safe, read-only metadata about a resume version.
 * Sensitive fields (storage paths, internal IDs for cloud providers)
 * are intentionally excluded from the API surface.</p>
 *
 * <p>This DTO is a pure data holder — mapping from entity to response
 * is performed by {@link com.interviewace.backend.mapper.ResumeMapper}.</p>
 *
 * @see com.interviewace.backend.entity.resume.ResumeVersion
 */
@Getter
@Setter
@NoArgsConstructor
public class ResumeVersionResponse {

    /**
     * The unique identifier of this resume version.
     */
    private Long id;

    /**
     * The version number (monotonically increasing per resume).
     */
    private Integer versionNumber;

    /**
     * The original filename as uploaded by the user.
     */
    private String originalFilename;

    /**
     * The storage backend used for this version (e.g., LOCAL, CLOUDINARY).
     */
    private String storageProvider;

    /**
     * The file size in bytes.
     */
    private Long fileSize;

    /**
     * The MIME type of the uploaded file.
     */
    private String mimeType;

    /**
     * The timestamp when the file was uploaded.
     */
    private LocalDateTime uploadedAt;

    /**
     * The current parsing status of this version.
     */
    private String parseStatus;

    /**
     * The timestamp when this version record was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when this version record was last updated.
     */
    private LocalDateTime updatedAt;

}
