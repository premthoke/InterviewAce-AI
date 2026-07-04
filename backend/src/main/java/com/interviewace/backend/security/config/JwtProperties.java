package com.interviewace.backend.security.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.time.Duration;

/**
 * Externalized JWT configuration bound to the "jwt" prefix in application.properties.
 *
 * Properties:
 *   jwt.secret      — Base64-encoded HMAC-SHA key (≥256 bits for HS256)
 *   jwt.expiration  — Token time-to-live in milliseconds
 *   jwt.issuer      — Token issuer claim (reserved for future use)
 *   jwt.audience    — Token audience claim (reserved for future use)
 *
 * The signing key is derived once from the Base64 secret and cached for the
 * lifetime of the application. All other components obtain the key through
 * {@link #getSigningKey()}.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration;
    private String issuer;
    private String audience;

    /** Cached SecretKey instance — decoded once, reused across all token operations. */
    private SecretKey signingKey;

    // ========================
    // Getters & Setters
    // ========================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
        // Invalidate cached key when secret changes (e.g., during testing)
        this.signingKey = null;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    // ========================
    // Derived Accessors
    // ========================

    /**
     * Returns the token time-to-live as a {@link Duration} for improved readability.
     */
    public Duration getExpirationDuration() {
        return Duration.ofMillis(expiration);
    }

    /**
     * Returns the HMAC-SHA signing key derived from the Base64-encoded secret.
     * The key is decoded once and cached for subsequent calls.
     *
     * @return the {@link SecretKey} used for signing and verifying JWTs
     */
    public SecretKey getSigningKey() {
        if (signingKey == null) {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }

}
