package com.interviewace.backend.security.jwt;

import com.interviewace.backend.security.config.JwtProperties;
import com.interviewace.backend.security.util.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

/**
 * Stateless JWT infrastructure component.
 *
 * Responsibilities:
 *   - Generate signed JWTs from a {@link UserDetails} principal
 *   - Parse and validate JWTs
 *   - Extract individual claims
 *
 * Design constraints:
 *   - No dependency on Controller, HttpServletRequest, HttpServletResponse, or Servlet API
 *   - Receives and returns plain Java objects only
 *   - Only called by AuthService — business services never interact with this class directly
 *   - Constructor injection only; fully unit-testable
 *
 * Claims stored in every token:
 *   sub  — username (email)
 *   role — user's role (USER / ADMIN)
 *   iat  — issued-at timestamp
 *   exp  — expiration timestamp
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ========================================================
    // Token Generation
    // ========================================================

    /**
     * Generates a signed JWT for the given authenticated principal.
     *
     * @param userDetails the authenticated user's details
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(JwtConstants.CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        log.info("JWT generated successfully for user: {}", userDetails.getUsername());

        return token;
    }

    // ========================================================
    // Claim Extraction
    // ========================================================

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT string
     * @return the username stored in the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT string
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from the token.
     *
     * @param token the JWT string
     * @return the complete {@link Claims} object
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts a single claim using the provided resolver function.
     *
     * @param token          the JWT string
     * @param claimsResolver function that extracts the desired claim from {@link Claims}
     * @param <T>            the return type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ========================================================
    // Token Validation
    // ========================================================

    /**
     * Validates a JWT against the provided user details.
     *
     * A token is valid if:
     *   1. It can be parsed and its signature is verified
     *   2. The subject matches the provided username
     *   3. The token has not expired
     *
     * @param token       the JWT string
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether the token has expired.
     *
     * @param token the JWT string
     * @return true if the token's expiration date is before the current time
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ========================================================
    // Configuration Accessors
    // ========================================================

    /**
     * Returns the configured token expiration as a {@link Duration}.
     *
     * @return the token time-to-live
     */
    public Duration getExpirationDuration() {
        return jwtProperties.getExpirationDuration();
    }

    /**
     * Returns the configured token expiration in milliseconds.
     * Used by AuthResponse to communicate the TTL to the client.
     *
     * @return the token time-to-live in milliseconds
     */
    public long getExpirationMillis() {
        return jwtProperties.getExpiration();
    }

    // ========================================================
    // Internal
    // ========================================================

    private SecretKey getSigningKey() {
        return jwtProperties.getSigningKey();
    }

}
