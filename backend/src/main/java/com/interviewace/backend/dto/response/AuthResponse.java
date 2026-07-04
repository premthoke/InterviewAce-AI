package com.interviewace.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Unified authentication response DTO.
 *
 * Used by both registration and login endpoints.
 * Token-related fields (token, tokenType, expiresIn) are only populated on login
 * and omitted from JSON when null via {@link JsonInclude}.
 */
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private boolean success;
    private String message;
    private String token;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime timestamp;

    /**
     * Factory method for successful operations that do not produce a token (e.g., registration).
     */
    public static AuthResponse success(String message) {
        return new AuthResponse(true, message, null, null, null, LocalDateTime.now());
    }

    /**
     * Factory method for successful login — includes JWT token metadata.
     *
     * @param message   human-readable success message
     * @param token     the signed JWT string
     * @param tokenType the token type (typically "Bearer")
     * @param expiresIn the token time-to-live in milliseconds
     */
    public static AuthResponse loginSuccess(String message, String token, String tokenType, long expiresIn) {
        return new AuthResponse(true, message, token, tokenType, expiresIn, LocalDateTime.now());
    }

    /**
     * Factory method for error responses.
     */
    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null, null, null, LocalDateTime.now());
    }

}
