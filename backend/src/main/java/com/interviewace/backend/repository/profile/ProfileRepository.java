package com.interviewace.backend.repository.profile;

import com.interviewace.backend.entity.profile.Profile;
import com.interviewace.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

    boolean existsByUser(User user);

}
