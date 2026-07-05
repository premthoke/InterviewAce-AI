package com.interviewace.backend.entity.profile;

import com.interviewace.backend.entity.base.BaseEntity;
import com.interviewace.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores professional and educational information for a user.
 *
 * <p>Each user owns exactly one profile (1:1 relationship).
 * The Profile entity is the owning side — it holds the {@code user_id} FK column.
 * The relationship is unidirectional: Profile references User, but User does not
 * reference Profile. This keeps the User entity focused solely on authentication.</p>
 *
 * <p>No cascade is configured. Deletion of related entities (Profile, Resume, etc.)
 * will be handled explicitly by a dedicated service in the application layer,
 * not silently by the database.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String headline;

    @Column(length = 500)
    private String bio;

    @Column(length = 15)
    private String phone;

    @Column(length = 150)
    private String location;

    @Column(length = 150)
    private String college;

    @Column(length = 100)
    private String degree;

    @Column(length = 100)
    private String branch;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

}
