package com.interviewace.backend.service.storage;

/**
 * Immutable result returned by {@link CloudinaryService#uploadResume} after a
 * successful file upload to Cloudinary.
 *
 * <p>Encapsulates the storage provider's response in a provider-agnostic
 * format. No Cloudinary SDK types, exceptions, or response maps leak past
 * this boundary.</p>
 *
 * <p>Implemented as a Java record for immutability and conciseness (Java 21+).</p>
 *
 * @param url      the publicly accessible HTTPS URL of the uploaded file
 * @param publicId Cloudinary's unique resource identifier
 *                 (used for delete, update, and admin operations)
 */
public record CloudinaryUploadResult(
        String url,
        String publicId
) {
}
