package com.interviewace.backend.service.storage;

/**
 * Abstraction for file storage operations.
 *
 * <p>Follows the Dependency Inversion Principle — the application depends
 * on this interface, not on any specific storage provider. Implementations
 * can be swapped (Cloudinary, S3, Azure, Local) without affecting the
 * rest of the codebase.</p>
 *
 * <p>Current implementation: {@code CloudinaryStorageService}.</p>
 *
 * <p>Future implementations:</p>
 * <ul>
 *     <li>{@code S3StorageService}</li>
 *     <li>{@code AzureStorageService}</li>
 *     <li>{@code LocalStorageService}</li>
 * </ul>
 *
 * @see StorageUploadResult
 */
public interface StorageService {

    /**
     * Uploads a file to the storage backend.
     *
     * @param fileBytes        the raw file content as a byte array
     * @param originalFilename the original filename as provided by the user
     * @param folder           the storage folder/prefix path
     * @return a {@link StorageUploadResult} containing the URL and resource identifier
     * @throws com.interviewace.backend.exception.StorageUploadException
     *         if the upload fails
     */
    StorageUploadResult upload(byte[] fileBytes, String originalFilename, String folder);

    /**
     * Deletes a file from the storage backend.
     *
     * <p>Used for compensating cleanup when the database save fails
     * after a successful upload, and for explicit user-initiated deletes.</p>
     *
     * @param resourceId the storage provider's unique resource identifier
     *                   (e.g., Cloudinary public ID)
     */
    void delete(String resourceId);

}
