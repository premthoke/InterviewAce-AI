package com.interviewace.backend.service.profile;

import com.interviewace.backend.dto.profile.ProfileRequest;
import com.interviewace.backend.dto.profile.ProfileResponse;
import com.interviewace.backend.entity.profile.Profile;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.exception.ProfileNotFoundException;
import com.interviewace.backend.mapper.ProfileMapper;
import com.interviewace.backend.repository.profile.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ProfileService}.
 *
 * <p>Manages the lifecycle of user profiles using upsert semantics.
 * Depends only on {@link ProfileRepository} and {@link ProfileMapper} —
 * no Spring Security imports, no {@code SecurityContextHolder} access.</p>
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Upsert logic: looks up an existing profile for the user.
     * If found, updates all fields. If not found, creates a new profile
     * and links it to the user.</p>
     */
    @Override
    @Transactional
    public ProfileResponse saveProfile(User user, ProfileRequest request) {

        Profile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new profile for user: email={}", user.getEmail());
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profileMapper.updateEntity(profile, request);
        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile saved successfully for user: email={}", user.getEmail());

        return profileMapper.toResponse(savedProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(User user) {

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Profile not found for user: email={}", user.getEmail());
                    return new ProfileNotFoundException(
                            "Profile not found. Please create your profile first."
                    );
                });

        return profileMapper.toResponse(profile);
    }

}
