package com.interviewace.backend.service.storage;

/**
 * Immutable result returned by {@link StorageService#upload} after a
 * successful file upload.
 *
 * <p>Encapsulates the storage provider's response in a provider-agnostic
 * format. No Cloudinary (or S3, Azure, etc.) types leak past this boundary.</p>
 *
 * @param url      the publicly accessible HTTPS URL of the uploaded file
 * @param publicId the storage provider's unique resource identifier
 *                 (used for delete, update, and admin operations)
 */
public record StorageUploadResult(
        String url,
        String publicId
) {
}
