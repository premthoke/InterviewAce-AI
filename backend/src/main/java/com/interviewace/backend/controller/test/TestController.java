package com.interviewace.backend.controller.test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test controller to verify JWT-based authentication.
 *
 * This endpoint is protected — only accessible with a valid JWT.
 * The authenticated user is automatically injected by Spring Security
 * via the SecurityContext populated by JwtAuthenticationFilter.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

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

}
