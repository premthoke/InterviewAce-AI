package com.interviewace.backend.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO representing a user's profile.
 *
 * <p>Includes {@code fullName} and {@code email} from the associated User entity
 * so the frontend can display profile information without a separate user lookup.
 * This DTO is a pure data holder — mapping from entity to response is performed
 * by {@link com.interviewace.backend.mapper.ProfileMapper}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileResponse {

    private Long id;

    private String fullName;

    private String email;

    private String headline;

    private String bio;

    private String phone;

    private String location;

    private String college;

    private String degree;

    private String branch;

    private Integer graduationYear;

    private String githubUrl;

    private String linkedinUrl;

    private String portfolioUrl;

    private String profileImage;

}
