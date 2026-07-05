package com.interviewace.backend.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO for creating or updating a user's profile.
 *
 * <p>Used by the {@code PUT /api/profile} upsert endpoint.
 * A single DTO serves both create and update because each user
 * owns exactly one profile — there is no distinction between
 * "first save" and "subsequent saves".</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileRequest {

    @Size(max = 100, message = "Headline must not exceed 100 characters")
    private String headline;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 150, message = "College name must not exceed 150 characters")
    private String college;

    @Size(max = 100, message = "Degree must not exceed 100 characters")
    private String degree;

    @Size(max = 100, message = "Branch must not exceed 100 characters")
    private String branch;

    @Min(value = 1950, message = "Graduation year must be 1950 or later")
    @Max(value = 2035, message = "Graduation year must be 2035 or earlier")
    private Integer graduationYear;

    @URL(message = "GitHub URL must be a valid URL")
    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    private String githubUrl;

    @URL(message = "LinkedIn URL must be a valid URL")
    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    private String linkedinUrl;

    @URL(message = "Portfolio URL must be a valid URL")
    @Size(max = 500, message = "Portfolio URL must not exceed 500 characters")
    private String portfolioUrl;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    private String profileImage;

}
