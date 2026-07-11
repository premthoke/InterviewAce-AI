package com.interviewace.backend.service.storage;

import com.cloudinary.Cloudinary;
import com.interviewace.backend.exception.StorageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Cloudinary implementation of {@link StorageService}.
 *
 * <p>All Cloudinary SDK interaction is encapsulated within this class.
 * No Cloudinary types, exceptions, or response maps leak past this boundary.
 * The rest of the application interacts only with {@link StorageService}
 * and {@link StorageUploadResult}.</p>
 *
 * <p>Files are uploaded as {@code raw} resource type (not image/video)
 * since resumes are PDFs. This ensures Cloudinary stores them without
 * any image-specific transformations.</p>
 */
@Service
public class CloudinaryStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;

    /**
     * Constructor injection for the Cloudinary SDK instance.
     *
     * @param cloudinary the configured Cloudinary SDK bean
     */
    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uploads the file bytes to Cloudinary under the specified folder
     * using {@code resource_type = "raw"} for non-media files (PDFs).</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public StorageUploadResult upload(byte[] fileBytes, String originalFilename, String folder) {
        log.info("Uploading file to Cloudinary: filename={}, folder={}, size={} bytes",
                originalFilename, folder, fileBytes.length);

        try {
            Map<String, Object> options = Map.of(
                    "resource_type", "raw",
                    "folder", folder,
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<String, Object> result = cloudinary.uploader().upload(fileBytes, options);

            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            log.info("Cloudinary upload successful: publicId={}", publicId);

            return new StorageUploadResult(secureUrl, publicId);

        } catch (Exception e) {
            log.error("Cloudinary upload failed: filename={}, error={}",
                    originalFilename, e.getMessage(), e);
            throw new StorageUploadException(
                    "Failed to upload file to cloud storage. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deletes the resource from Cloudinary by its public ID.
     * Uses {@code resource_type = "raw"} to match the upload type.
     * Failures are logged but not re-thrown — compensating deletes
     * are best-effort operations.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public void delete(String resourceId) {
        log.info("Deleting file from Cloudinary: publicId={}", resourceId);

        try {
            Map<String, Object> options = Map.of("resource_type", "raw");
            cloudinary.uploader().destroy(resourceId, options);
            log.info("Cloudinary delete successful: publicId={}", resourceId);

        } catch (Exception e) {
            // Compensating deletes are best-effort — log and move on.
            // A scheduled cleanup job can reconcile orphaned files later.
            log.error("Cloudinary delete failed (best-effort): publicId={}, error={}",
                    resourceId, e.getMessage(), e);
        }
    }

}
