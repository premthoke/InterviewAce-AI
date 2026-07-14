package com.interviewace.backend.service.resume;

import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.user.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestrates the resume upload business workflow.
 *
 * <p>This is a <b>workflow service</b>, not a domain query service.
 * It coordinates multiple concerns — file validation, cloud storage,
 * entity persistence, and version management — into one cohesive
 * business process.</p>
 *
 * <p>Separation from {@link ResumeService} follows the Single Responsibility
 * Principle:</p>
 * <ul>
 *     <li>{@link ResumeService} — domain queries (read, list, delete)</li>
 *     <li>{@code ResumeWorkflowService} — business processes (upload workflow)</li>
 * </ul>
 *
 * <p>The {@link MultipartFile} parameter is accepted at this boundary because
 * the workflow is the first layer that decomposes it into plain data
 * ({@code byte[]}, filename, contentType) before delegating to
 * framework-agnostic services like
 * {@link com.interviewace.backend.service.storage.CloudinaryService}.</p>
 *
 * @see ResumeService
 * @see com.interviewace.backend.service.storage.CloudinaryService
 */
public interface ResumeWorkflowService {

    /**
     * Executes the complete resume upload workflow.
     *
     * <p>Workflow steps:</p>
     * <ol>
     *     <li>Validate the uploaded file (PDF only, ≤5MB, non-empty)</li>
     *     <li>Extract byte[], filename, contentType from MultipartFile</li>
     *     <li>Compute SHA-256 checksum</li>
     *     <li>Upload to Cloudinary</li>
     *     <li>Find or create the Resume aggregate for the user</li>
     *     <li>Determine the next version number</li>
     *     <li>Create and save the ResumeVersion entity</li>
     *     <li>Update Resume.currentVersion</li>
     *     <li>Return the saved version as a response DTO</li>
     * </ol>
     *
     * <p>If Cloudinary upload succeeds but database persistence fails,
     * a compensating delete removes the orphaned Cloudinary file before
     * re-throwing the exception.</p>
     *
     * @param user the authenticated user who owns this resume
     * @param file the uploaded PDF file
     * @return the created resume version as a response DTO
     * @throws com.interviewace.backend.exception.InvalidResumeFileException
     *         if the file fails validation (not PDF, too large, empty)
     * @throws com.interviewace.backend.exception.ResumeUploadException
     *         if the upload workflow fails after Cloudinary upload succeeds
     * @throws com.interviewace.backend.exception.StorageUploadException
     *         if the Cloudinary upload itself fails
     */
    ResumeVersionResponse uploadResume(User user, MultipartFile file);

}
