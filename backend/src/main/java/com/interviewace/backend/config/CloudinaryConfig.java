package com.interviewace.backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Spring configuration that creates and exposes the {@link Cloudinary} SDK
 * instance as a managed bean.
 *
 * <p>The SDK is initialized once using credentials from
 * {@link CloudinaryProperties} and reused across all injection points.
 * All communication uses HTTPS ({@code secure = true}).</p>
 *
 * <p>This is the ONLY class that directly references the Cloudinary SDK
 * for configuration purposes. All runtime usage goes through
 * {@link com.interviewace.backend.service.storage.CloudinaryService}.</p>
 */
@Configuration
public class CloudinaryConfig {

    /**
     * Creates the Cloudinary SDK instance.
     *
     * @param properties the Cloudinary configuration properties
     * @return a configured Cloudinary instance
     */
    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        return new Cloudinary(Map.of(
                "cloud_name", properties.getCloudName(),
                "api_key", properties.getApiKey(),
                "api_secret", properties.getApiSecret(),
                "secure", true
        ));
    }

}
