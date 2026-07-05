package com.interviewace.backend.mapper;

import com.interviewace.backend.dto.profile.ProfileRequest;
import com.interviewace.backend.dto.profile.ProfileResponse;
import com.interviewace.backend.entity.profile.Profile;
import org.springframework.stereotype.Component;

/**
 * Maps between Profile entities and Profile DTOs.
 *
 * <p>Centralizes all Profile mapping logic in one place, keeping DTOs as
 * pure data holders. This mapper can be replaced with MapStruct or a similar
 * library in the future without touching DTOs or entities.</p>
 */
@Component
public class ProfileMapper {

    /**
     * Converts a Profile entity to a ProfileResponse DTO.
     *
     * <p>Pulls {@code fullName} and {@code email} from the associated User entity
     * so the frontend can render profile pages without an additional user lookup.</p>
     *
     * @param profile the Profile entity to convert
     * @return a fully populated ProfileResponse
     */
    public ProfileResponse toResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();

        response.setId(profile.getId());
        response.setFullName(profile.getUser().getFullName());
        response.setEmail(profile.getUser().getEmail());
        response.setHeadline(profile.getHeadline());
        response.setBio(profile.getBio());
        response.setPhone(profile.getPhone());
        response.setLocation(profile.getLocation());
        response.setCollege(profile.getCollege());
        response.setDegree(profile.getDegree());
        response.setBranch(profile.getBranch());
        response.setGraduationYear(profile.getGraduationYear());
        response.setGithubUrl(profile.getGithubUrl());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setPortfolioUrl(profile.getPortfolioUrl());
        response.setProfileImage(profile.getProfileImage());

        return response;
    }

    /**
     * Applies fields from a ProfileRequest onto an existing Profile entity.
     *
     * <p>Used during both create and update flows. On create, the entity is new
     * and all fields are set for the first time. On update, existing field values
     * are overwritten with the incoming request data.</p>
     *
     * @param profile the target entity to update
     * @param request the incoming request data
     */
    public void updateEntity(Profile profile, ProfileRequest request) {
        profile.setHeadline(request.getHeadline());
        profile.setBio(request.getBio());
        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setCollege(request.getCollege());
        profile.setDegree(request.getDegree());
        profile.setBranch(request.getBranch());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setPortfolioUrl(request.getPortfolioUrl());
        profile.setProfileImage(request.getProfileImage());
    }

}
