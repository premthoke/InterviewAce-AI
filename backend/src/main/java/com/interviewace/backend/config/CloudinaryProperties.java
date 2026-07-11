package com.interviewace.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for Cloudinary integration.
 *
 * <p>Binds to the {@code cloudinary.*} prefix in {@code application.properties}.
 * Sensitive values ({@code apiKey}, {@code apiSecret}) should be supplied via
 * environment variables — never hardcoded.</p>
 */
@Configuration
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    /**
     * Root folder in Cloudinary where resume files are stored.
     * Defaults to {@code resumes}.
     */
    private String folder = "resumes";

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

}
