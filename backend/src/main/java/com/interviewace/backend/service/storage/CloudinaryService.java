package com.interviewace.backend.service.storage;

/**
 * Defines cloud storage operations for resume files.
 *
 * <p>This interface deliberately avoids any dependency on Spring MVC types
 * (e.g., {@code MultipartFile}). It accepts plain Java types so that
 * implementations remain reusable across contexts — controllers, workflow
 * services, scheduled jobs, CLI tools, and unit tests.</p>
 *
 * <p>Current implementation: {@link CloudinaryServiceImpl}.</p>
 *
 * @see CloudinaryUploadResult
 */
public interface CloudinaryService {

    /**
     * Uploads a resume file to Cloudinary.
     *
     * <p>The caller is responsible for extracting the raw bytes, filename,
     * and content type from whatever source (e.g., {@code MultipartFile}).
     * This keeps the service layer framework-agnostic.</p>
     *
     * @param fileBytes        the raw file content as a byte array
     * @param originalFilename the original filename as provided by the user
     * @param contentType      the MIME type of the file (e.g., {@code application/pdf})
     * @return a {@link CloudinaryUploadResult} containing the URL and public ID
     * @throws com.interviewace.backend.exception.StorageUploadException
     *         if the upload fails
     */
    CloudinaryUploadResult uploadResume(byte[] fileBytes, String originalFilename, String contentType);

    /**
     * Deletes a resume file from Cloudinary.
     *
     * <p>Used for compensating cleanup when the database save fails
     * after a successful upload, and for explicit user-initiated deletes.</p>
     *
     * @param publicId Cloudinary's unique resource identifier for the file
     */
    void deleteResume(String publicId);

}
