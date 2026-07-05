package com.interviewace.backend.dto.resume;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO representing a user's resume aggregate.
 *
 * <p>In Phase 5.1 this is minimal — just the aggregate root fields.
 * Future phases will add {@code currentVersion} (Phase 5.2) and
 * {@code totalVersions} (Phase 5.2, derived via COUNT query).</p>
 *
 * <p>This DTO is a pure data holder — mapping from entity to response
 * is performed by {@link com.interviewace.backend.mapper.ResumeMapper}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ResumeResponse {

    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
