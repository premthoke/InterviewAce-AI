package com.interviewace.backend.service.profile;

import com.interviewace.backend.dto.profile.ProfileRequest;
import com.interviewace.backend.dto.profile.ProfileResponse;
import com.interviewace.backend.entity.user.User;

/**
 * Defines operations for managing user profiles.
 *
 * <p>All methods receive the authenticated {@link User} as a parameter.
 * The service layer never accesses {@code SecurityContextHolder} directly —
 * the controller is responsible for extracting the user from the security context
 * and passing it here. This keeps the service framework-agnostic and reusable
 * regardless of the authentication mechanism (JWT, OAuth2, SSO, etc.).</p>
 */
public interface ProfileService {

    /**
     * Creates or updates the profile for the given user.
     *
     * <p>If the user already has a profile, the existing profile is updated
     * with the incoming request data. If no profile exists, a new one is created.
     * This upsert behavior allows the frontend to always call {@code PUT /api/profile}
     * without needing to check whether a profile exists first.</p>
     *
     * @param user    the authenticated user who owns this profile
     * @param request the profile data to save
     * @return the saved profile as a response DTO
     */
    ProfileResponse saveProfile(User user, ProfileRequest request);

    /**
     * Retrieves the profile for the given user.
     *
     * @param user the authenticated user whose profile is requested
     * @return the user's profile as a response DTO
     * @throws com.interviewace.backend.exception.ProfileNotFoundException
     *         if the user has not yet created a profile
     */
    ProfileResponse getMyProfile(User user);

}
