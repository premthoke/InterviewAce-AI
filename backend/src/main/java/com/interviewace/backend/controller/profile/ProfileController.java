package com.interviewace.backend.controller.profile;

import com.interviewace.backend.dto.profile.ProfileRequest;
import com.interviewace.backend.dto.profile.ProfileResponse;
import com.interviewace.backend.dto.response.ApiResponse;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.security.user.CustomUserPrincipal;
import com.interviewace.backend.service.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the authenticated user's profile.
 *
 * <p>Exposes two endpoints:
 * <ul>
 *   <li>{@code GET /api/profile} — retrieve the current user's profile</li>
 *   <li>{@code PUT /api/profile} — create or update the current user's profile (upsert)</li>
 * </ul>
 *
 * <p>The authenticated {@link User} is extracted from the {@link Authentication} object
 * in the controller and passed to the service. The service layer never accesses
 * Spring Security directly.</p>
 */
@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Endpoints for managing user profile information")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Retrieves the authenticated user's profile.
     *
     * @param authentication the current authentication containing the user principal
     * @return the user's profile wrapped in an {@link ApiResponse}
     */
    @GetMapping
    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the currently authenticated user. "
                    + "Returns 404 if the user has not created a profile yet."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Profile not found — user has not created a profile yet"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<ApiResponse> getMyProfile(Authentication authentication) {

        User user = extractUser(authentication);
        ProfileResponse profile = profileService.getMyProfile(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Profile retrieved successfully", profile)
        );
    }

    /**
     * Creates or updates the authenticated user's profile (upsert).
     *
     * <p>If the user already has a profile, the existing profile is updated.
     * If no profile exists, a new one is created. The frontend can always call
     * this endpoint without checking whether a profile exists first.</p>
     *
     * @param authentication the current authentication containing the user principal
     * @param request        the profile data to save
     * @return the saved profile wrapped in an {@link ApiResponse}
     */
    @PutMapping
    @Operation(
            summary = "Save my profile",
            description = "Creates a new profile if one does not exist, or updates the existing profile. "
                    + "This upsert behavior means the frontend never needs to check profile existence."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile saved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error — invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — missing or invalid JWT token"
            )
    })
    public ResponseEntity<ApiResponse> saveProfile(Authentication authentication,
                                                   @Valid @RequestBody ProfileRequest request) {

        User user = extractUser(authentication);
        ProfileResponse profile = profileService.saveProfile(user, request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Profile saved successfully", profile)
        );
    }

    /**
     * Extracts the {@link User} entity from the Spring Security {@link Authentication} object.
     *
     * <p>This is the only place in the Profile module that interacts with Spring Security.
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
