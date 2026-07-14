package com.interviewace.backend.controller.resume;

import com.interviewace.backend.dto.response.ApiResponse;
import com.interviewace.backend.dto.resume.ResumeResponse;
import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.security.user.CustomUserPrincipal;
import com.interviewace.backend.service.resume.ResumeService;
import com.interviewace.backend.service.resume.ResumeWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for managing the authenticated user's resume and its versions.
 *
 * <p>Phase 5.1 endpoints:</p>
 * <ul>
 *   <li>{@code GET /api/resume} — retrieve the current user's resume</li>
 *   <li>{@code DELETE /api/resume} — delete the current user's resume</li>
 * </ul>
 *
 * <p>Phase 5.2 endpoints:</p>
 * <ul>
 *   <li>{@code GET /api/resume/versions} — list all resume versions</li>
 *   <li>{@code GET /api/resume/current} — get the current (latest) version</li>
 * </ul>
 *
 * <p>Phase 5.3B endpoints:</p>
 * <ul>
 *   <li>{@code POST /api/resume/upload} — upload a new resume version</li>
 * </ul>
 *
 * <p>The authenticated {@link User} is extracted from the {@link Authentication} object
 * in the controller and passed to the service. The service layer never accesses
 * Spring Security directly.</p>
 */
@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume", description = "Endpoints for managing user resume and versions")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    private final ResumeService resumeService;
    private final ResumeWorkflowService resumeWorkflowService;

    public ResumeController(ResumeService resumeService,
                            ResumeWorkflowService resumeWorkflowService) {
        this.resumeService = resumeService;
        this.resumeWorkflowService = resumeWorkflowService;
    }

    /* ------------------------------------------------------------------ */
    /*  Phase 5.3B — Resume Upload Endpoint                                */
    /* ------------------------------------------------------------------ */

    /**
     * Uploads a new resume version for the authenticated user.
     *
     * <p>This is the first end-to-end business workflow in the application.
     * It coordinates file validation, Cloudinary upload, Resume/ResumeVersion
     * persistence, and version management in a single request.</p>
     *
     * <p>If the user has no existing Resume, one is created lazily.
     * Each upload creates a new {@code ResumeVersion} and updates
     * the {@code currentVersion} pointer on the Resume aggregate.</p>
     *
     * @param authentication the current authentication containing the user principal
     * @param file           the PDF file to upload (max 5MB)
     * @return the created resume version wrapped in an {@link ApiResponse}
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a resume",
            description = "Uploads a PDF resume file and creates a new resume version. "
                    + "If this is the user's first upload, a Resume aggregate is created automatically. "
                    + "Each upload increments the version number and updates the current version pointer. "
                    + "Accepts only PDF files up to 5MB."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Resume version created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file — not a PDF, exceeds 5MB, or empty"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Upload failed — Cloudinary or database error"
            )
    })
    public ResponseEntity<ApiResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        User user = extractUser(authentication);
        log.info("POST /api/resume/upload — user: {}", user.getEmail());

        ResumeVersionResponse version = resumeWorkflowService.uploadResume(user, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Resume uploaded successfully", version));
    }

    /* ------------------------------------------------------------------ */
    /*  Phase 5.1 — Resume Aggregate Endpoints                             */
    /* ------------------------------------------------------------------ */

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
        log.info("GET /api/resume — user: {}", user.getEmail());

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
        log.info("DELETE /api/resume — user: {}", user.getEmail());

        resumeService.deleteResume(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Resume deleted successfully", null)
        );
    }

    /* ------------------------------------------------------------------ */
    /*  Phase 5.2 — Resume Version Endpoints                               */
    /* ------------------------------------------------------------------ */

    /**
     * Retrieves all resume versions for the authenticated user.
     *
     * <p>Versions are returned in descending order by version number
     * (newest first). Returns an empty list if the resume exists but
     * has no versions yet.</p>
     *
     * @param authentication the current authentication containing the user principal
     * @return list of resume versions wrapped in an {@link ApiResponse}
     */
    @GetMapping("/versions")
    @Operation(
            summary = "Get all resume versions",
            description = "Returns all resume versions for the currently authenticated user, "
                    + "ordered by version number descending (newest first). "
                    + "Returns 404 if the user has not uploaded a resume yet."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resume versions retrieved successfully"
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
    public ResponseEntity<ApiResponse> getVersions(Authentication authentication) {

        User user = extractUser(authentication);
        log.info("GET /api/resume/versions — user: {}", user.getEmail());

        List<ResumeVersionResponse> versions = resumeService.getVersions(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Resume versions retrieved successfully", versions)
        );
    }

    /**
     * Retrieves the current (latest) resume version for the authenticated user.
     *
     * <p>Resolves the current version via the {@code currentVersion} pointer
     * on the Resume aggregate root, with a fallback to the highest version number.</p>
     *
     * @param authentication the current authentication containing the user principal
     * @return the current resume version wrapped in an {@link ApiResponse}
     */
    @GetMapping("/current")
    @Operation(
            summary = "Get current resume version",
            description = "Returns the current (latest) resume version for the authenticated user. "
                    + "Returns 404 if the user has no resume or no versions exist."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current resume version retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resume or version not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<ApiResponse> getCurrentVersion(Authentication authentication) {

        User user = extractUser(authentication);
        log.info("GET /api/resume/current — user: {}", user.getEmail());

        ResumeVersionResponse currentVersion = resumeService.getCurrentVersion(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Current resume version retrieved successfully", currentVersion)
        );
    }

    /* ------------------------------------------------------------------ */
    /*  Private Helpers                                                     */
    /* ------------------------------------------------------------------ */

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
