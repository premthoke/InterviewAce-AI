package com.interviewace.backend.security.util;

/**
 * Centralized constants for JWT operations.
 *
 * Eliminates magic strings across the security layer. These constants are
 * referenced by JwtTokenService and will be reused by JwtAuthenticationFilter
 * in the next milestone.
 */
public final class JwtConstants {

    private JwtConstants() {
        // Prevent instantiation — constants-only class
    }

    /** Prefix prepended to the JWT in the Authorization header value. */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** HTTP header name used to transmit the JWT. */
    public static final String HEADER = "Authorization";

    /** Token type returned in authentication responses. */
    public static final String TOKEN_TYPE = "Bearer";

    /** Custom claim key for the user's role. */
    public static final String CLAIM_ROLE = "role";

    /** Token issuer identifier (iss claim). */
    public static final String ISSUER = "InterviewAce";

    /** Token audience identifier (aud claim). */
    public static final String AUDIENCE = "InterviewAce-Client";

}
