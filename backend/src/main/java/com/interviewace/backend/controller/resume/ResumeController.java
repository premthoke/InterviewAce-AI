package com.interviewace.backend.controller.resume;

import com.interviewace.backend.dto.response.ApiResponse;
import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.security.user.CustomUserPrincipal;
import com.interviewace.backend.service.resume.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the authenticated user's resume.
 *
 * <p>Phase 5.1 exposes two endpoints:
 * <ul>
 *   <li>{@code GET /api/resume} — retrieve the current user's resume</li>
 *   <li>{@code DELETE /api/resume} — delete the current user's resume</li>
 * </ul>
 *
 * <p>Since the Resume aggregate is created lazily (only on first upload,
 * introduced in Phase 5.3), both endpoints return 404 until the user
 * has uploaded at least one resume.</p>
 *
 * <p>The authenticated {@link User} is extracted from the {@link Authentication} object
 * in the controller and passed to the service. The service layer never accesses
 * Spring Security directly.</p>
 */
@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume", description = "Endpoints for managing user resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Retrieves the authenticated user's resume.
     *
     * @param authentication the current authentication containing the user principal
     * @return the user's resume wrapped in an {@link ApiResponse}
     */
    @GetMapping
    @Operation(
            summary = "Get my resume",
            description = "Returns the resume of the currently authenticated user. "
                    + "Returns 404 if the user has not uploaded a resume yet."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resume retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resume not found — user has not uploaded a resume yet"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<ApiResponse> getMyResume(Authentication authentication) {

        User user = extractUser(authentication);
        ResumeResponse resume = resumeService.getResume(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Resume retrieved successfully", resume)
        );
    }

    /**
     * Deletes the authenticated user's resume and all associated data.
     *
     * @param authentication the current authentication containing the user principal
     * @return a success message wrapped in an {@link ApiResponse}
     */
    @DeleteMapping
    @Operation(
            summary = "Delete my resume",
            description = "Deletes the resume aggregate and all associated versions, analyses, "
                    + "and cloud storage files for the currently authenticated user. "
                    + "Returns 404 if the user has no resume to delete."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resume deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resume not found — nothing to delete"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<ApiResponse> deleteMyResume(Authentication authentication) {

        User user = extractUser(authentication);
        resumeService.deleteResume(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Resume deleted successfully", null)
        );
    }

    /**
     * Extracts the {@link User} entity from the Spring Security {@link Authentication} object.
     *
     * <p>This is the only place in the Resume module that interacts with Spring Security.
     * The extracted User is then passed to the service layer as a plain parameter.</p>
     *
     * @param authentication the current authentication
     * @return the authenticated User entity
     */
    private User extractUser(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        return principal.getUser();
    }

}
