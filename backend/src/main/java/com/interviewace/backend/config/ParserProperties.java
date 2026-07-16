package com.interviewace.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for the PDF parsing layer.
 *
 * <p>Binds to the {@code interviewace.parser.*} prefix in
 * {@code application.properties}. Provides configurable limits
 * for the parsing pipeline without hardcoding values in the service.</p>
 *
 * <p>Follows the same pattern as {@link CloudinaryProperties}.</p>
 *
 * @see CloudinaryProperties
 */
@Configuration
@ConfigurationProperties(prefix = "interviewace.parser")
public class ParserProperties {

    /**
     * Maximum time (in seconds) allowed for a single parse operation.
     *
     * <p>If a parse exceeds this duration, it may be considered stale
     * and eligible for cleanup by a future scheduled job.</p>
     */
    private int timeoutSeconds = 60;

    /**
     * Maximum PDF file size (in bytes) that the parser will accept.
     *
     * <p>This is a safety guard independent of the upload size limit.
     * Set slightly larger than the upload limit to account for any
     * future changes to upload constraints.</p>
     *
     * <p>Default: 10MB (10,485,760 bytes).</p>
     */
    private long maxParseSizeBytes = 10_485_760L;

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getMaxParseSizeBytes() {
        return maxParseSizeBytes;
    }

    public void setMaxParseSizeBytes(long maxParseSizeBytes) {
        this.maxParseSizeBytes = maxParseSizeBytes;
    }
}
