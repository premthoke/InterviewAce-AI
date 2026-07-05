package com.interviewace.backend.repository.resume;

import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByUser(User user);

    boolean existsByUser(User user);

}
