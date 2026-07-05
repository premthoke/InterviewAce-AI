package com.interviewace.backend.service.resume;

import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.entity.user.User;

/**
 * Defines operations for managing the Resume aggregate.
 *
 * <p>All methods receive the authenticated {@link User} as a parameter.
 * The service layer never accesses {@code SecurityContextHolder} directly —
 * the controller is responsible for extracting the user from the security context
 * and passing it here. This keeps the service framework-agnostic and reusable
 * regardless of the authentication mechanism (JWT, OAuth2, SSO, etc.).</p>
 *
 * <p>Phase 5.1 provides read and delete operations only. Upload (which triggers
 * lazy creation of the Resume aggregate) will be added in Phase 5.3.</p>
 */
public interface ResumeService {

    /**
     * Retrieves the resume aggregate for the given user.
     *
     * <p>Since the Resume is created lazily (only on first upload),
     * this method throws {@link com.interviewace.backend.exception.ResumeNotFoundException}
     * if the user has not uploaded a resume yet.</p>
     *
     * @param user the authenticated user whose resume is requested
     * @return the user's resume as a response DTO
     * @throws com.interviewace.backend.exception.ResumeNotFoundException
     *         if the user has not yet uploaded a resume
     */
    ResumeResponse getResume(User user);

    /**
     * Deletes the resume aggregate and all associated data for the given user.
     *
     * <p>In Phase 5.1 this deletes the Resume entity only. In later phases,
     * this will also delete all ResumeVersions, ResumeAnalyses, and
     * Cloudinary files.</p>
     *
     * @param user the authenticated user whose resume should be deleted
     * @throws com.interviewace.backend.exception.ResumeNotFoundException
     *         if the user has no resume to delete
     */
    void deleteResume(User user);

}
