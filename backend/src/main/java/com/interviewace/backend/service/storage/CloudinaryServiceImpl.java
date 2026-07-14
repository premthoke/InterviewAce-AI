package com.interviewace.backend.service.storage;

import com.cloudinary.Cloudinary;
import com.interviewace.backend.config.CloudinaryProperties;
import com.interviewace.backend.exception.StorageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Cloudinary implementation of {@link CloudinaryService}.
 *
 * <p>All Cloudinary SDK interaction is encapsulated within this class.
 * No Cloudinary types, exceptions, or response maps leak past this boundary.
 * The rest of the application interacts only with {@link CloudinaryService}
 * and {@link CloudinaryUploadResult}.</p>
 *
 * <p>Files are uploaded as {@code raw} resource type (not image/video)
 * since resumes are PDFs. This ensures Cloudinary stores them without
 * any image-specific transformations.</p>
 *
 * <p><b>Note:</b> This service does NOT accept {@code MultipartFile} —
 * it receives plain {@code byte[]} and metadata to remain framework-agnostic
 * and reusable across controllers, workflow services, scheduled jobs,
 * and unit tests.</p>
 */
@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    /**
     * Constructor injection for the Cloudinary SDK instance and properties.
     *
     * @param cloudinary the configured Cloudinary SDK bean
     * @param properties the Cloudinary configuration properties (for folder name, etc.)
     */
    public CloudinaryServiceImpl(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uploads the file bytes to Cloudinary under the configured resume folder
     * using {@code resource_type = "raw"} for non-media files (PDFs).</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public CloudinaryUploadResult uploadResume(byte[] fileBytes, String originalFilename, String contentType) {
        log.info("Uploading file to Cloudinary: filename={}, folder={}, size={} bytes",
                originalFilename, properties.getFolder(), fileBytes.length);

        try {
            Map<String, Object> options = Map.of(
                    "resource_type", "raw",
                    "folder", properties.getFolder(),
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<String, Object> result = cloudinary.uploader().upload(fileBytes, options);

            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            log.info("Upload successful: publicId={}", publicId);

            return new CloudinaryUploadResult(secureUrl, publicId);

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
    public void deleteResume(String publicId) {
        log.info("Deleting file from Cloudinary: publicId={}", publicId);

        try {
            Map<String, Object> options = Map.of("resource_type", "raw");
            cloudinary.uploader().destroy(publicId, options);
            log.info("Delete successful: publicId={}", publicId);

        } catch (Exception e) {
            // Compensating deletes are best-effort — log and move on.
            // A scheduled cleanup job can reconcile orphaned files later.
            log.error("Cloudinary delete failed (best-effort): publicId={}, error={}",
                    publicId, e.getMessage(), e);
        }
    }

}
