package com.interviewace.backend.controller.test;

import com.interviewace.backend.dto.response.ApiResponse;
import com.interviewace.backend.exception.InvalidResumeFileException;
import com.interviewace.backend.service.storage.CloudinaryService;
import com.interviewace.backend.service.storage.CloudinaryUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test controller for verifying integrations independently.
 *
 * <p>Contains endpoints for JWT verification and Cloudinary upload testing.
 * All endpoints require authentication.</p>
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Temporary endpoints for integration verification")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /** Maximum file size allowed: 5MB. */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** The only allowed MIME type for resume uploads. */
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    private final CloudinaryService cloudinaryService;

    public TestController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Returns the authenticated user's email and role.
     *
     * Uses the {@link Authentication} object that Spring Security injects
     * from the SecurityContextHolder — no manual JWT parsing needed.
     *
     * @param authentication the current authenticated principal
     * @return email and role of the authenticated user
     */
    @GetMapping("/me")
    @Operation(
            summary = "Verify JWT authentication",
            description = "Returns the email and role extracted from the JWT token. "
                    + "Useful for verifying that authentication is working correctly."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "JWT is valid — returns authenticated user info"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {

        String email = authentication.getName();

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        Map<String, String> response = new LinkedHashMap<>();
        response.put("email", email);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // Temporary endpoint for Cloudinary verification.
    // Will be removed after Phase 5.3B.
    // =========================================================================

    /**
     * Uploads a PDF file to Cloudinary and returns the URL and public ID.
     *
     * <p><b>This is a temporary test endpoint.</b> It verifies Cloudinary
     * integration independently — nothing is stored in the database.
     * Will be removed after Phase 5.3B when the Resume workflow is connected.</p>
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Only {@code application/pdf} files are accepted</li>
     *     <li>Maximum file size: 5MB</li>
     *     <li>File must not be empty</li>
     * </ul>
     *
     * @param file the PDF file to upload
     * @return the Cloudinary URL and public ID wrapped in an {@link ApiResponse}
     */
    @PostMapping(value = "/cloudinary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Test Cloudinary upload",
            description = "Uploads a PDF file to Cloudinary and returns the URL and public ID. "
                    + "Nothing is stored in the database — this endpoint is strictly for "
                    + "verifying that the Cloudinary integration works. "
                    + "Accepts only PDF files up to 5MB. "
                    + "**Temporary endpoint — will be removed after Phase 5.3B.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File uploaded successfully — returns URL and public ID"
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
                    description = "Cloudinary upload failed — storage provider error"
            )
    })
    public ResponseEntity<ApiResponse> testCloudinaryUpload(
            @RequestParam("file") MultipartFile file) {

        // --- Validation (MultipartFile stays in the controller layer) ---
        validateResumeFile(file);

        // --- Extract plain data from MultipartFile ---
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes: {}", e.getMessage(), e);
            throw new InvalidResumeFileException("Failed to read uploaded file. Please try again.");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // --- Delegate to framework-agnostic service ---
        CloudinaryUploadResult result = cloudinaryService.uploadResume(
                fileBytes, originalFilename, contentType
        );

        // --- Return result (nothing stored in DB) ---
        Map<String, String> data = new LinkedHashMap<>();
        data.put("url", result.url());
        data.put("publicId", result.publicId());

        return ResponseEntity.ok(
                new ApiResponse(true, "Cloudinary upload test successful", data)
        );
    }

    /**
     * Validates the uploaded resume file.
     *
     * <p>Validation checks:</p>
     * <ol>
     *     <li>File must not be null or empty</li>
     *     <li>MIME type must be {@code application/pdf}</li>
     *     <li>File size must not exceed 5MB</li>
     * </ol>
     *
     * <p>This validation lives in the controller layer because it operates
     * on {@code MultipartFile} — a Spring MVC concept. The service layer
     * remains framework-agnostic.</p>
     *
     * @param file the uploaded file to validate
     * @throws InvalidResumeFileException if any validation check fails
     */
    private void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Invalid file type: file is empty or missing");
            throw new InvalidResumeFileException("Resume file is required and must not be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            log.warn("Invalid file type: received contentType={}, expected={}",
                    contentType, ALLOWED_CONTENT_TYPE);
            throw new InvalidResumeFileException(
                    "Only PDF files are accepted. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Invalid file type: size={} bytes exceeds maximum={} bytes",
                    file.getSize(), MAX_FILE_SIZE);
            throw new InvalidResumeFileException(
                    "File size exceeds the maximum allowed size of 5MB.");
        }
    }

}
