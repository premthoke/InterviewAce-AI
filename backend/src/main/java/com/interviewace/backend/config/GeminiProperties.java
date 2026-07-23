package com.interviewace.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized configuration for the Google Gemini AI integration.
 *
 * <p>Binds properties prefixed with {@code interviewace.gemini.*} from
 * {@code application.properties} or environment variables. All properties
 * have sensible defaults for development — only {@code apiKey} must be
 * provided externally.</p>
 *
 * <p>Configuration reference:</p>
 * <table>
 *     <tr><th>Property</th><th>Default</th><th>Description</th></tr>
 *     <tr><td>{@code api-key}</td><td>(env)</td><td>Gemini API key</td></tr>
 *     <tr><td>{@code model-name}</td><td>{@code gemini-2.5-flash}</td><td>Model identifier</td></tr>
 *     <tr><td>{@code temperature}</td><td>{@code 0.3}</td><td>Sampling temperature (low for structured output)</td></tr>
 *     <tr><td>{@code max-output-tokens}</td><td>{@code 4096}</td><td>Max tokens in completion</td></tr>
 *     <tr><td>{@code timeout-seconds}</td><td>{@code 30}</td><td>HTTP request timeout</td></tr>
 *     <tr><td>{@code max-retries}</td><td>{@code 2}</td><td>Retry attempts for transient errors</td></tr>
 *     <tr><td>{@code prompt-version}</td><td>{@code 1.0.0}</td><td>Current prompt template version</td></tr>
 * </table>
 *
 * @see com.interviewace.backend.entity.resume.ResumeAnalysis
 */
@Component
@ConfigurationProperties(prefix = "interviewace.gemini")
@Getter
@Setter
public class GeminiProperties {

    /**
     * API key for authenticating with Google Gemini.
     *
     * <p>Typically injected via the {@code GEMINI_API_KEY} environment variable.
     * Must be provided — the application will fail to call the Gemini API
     * without a valid key.</p>
     */
    private String apiKey;

    /**
     * The Gemini model identifier to use for resume analysis.
     *
     * <p>Stored on every {@code ResumeAnalysis} entity for traceability.
     * Changing this value affects all new analyses without requiring
     * code changes.</p>
     */
    private String modelName = "gemini-2.5-flash";

    /**
     * Sampling temperature for the Gemini model.
     *
     * <p>Low temperature (0.3) is used for consistency and reproducibility.
     * Resume analysis should be factual, not creative. Higher temperatures
     * risk hallucinated skills or inconsistent scoring.</p>
     */
    private Double temperature = 0.3;

    /**
     * Maximum number of tokens in the Gemini completion response.
     *
     * <p>4096 is sufficient for the structured JSON response, which
     * typically consumes 800–1,500 tokens.</p>
     */
    private Integer maxOutputTokens = 4096;

    /**
     * HTTP request timeout in seconds for Gemini API calls.
     *
     * <p>After this duration, the call is aborted and classified as
     * {@code AnalysisFailureReason.TIMEOUT}.</p>
     */
    private Integer timeoutSeconds = 30;

    /**
     * Maximum retry attempts for transient Gemini errors.
     *
     * <p>Applies to HTTP 429 (rate limit), 500/502/503 (server errors),
     * and network timeouts. Non-retryable errors (400, invalid JSON)
     * fail immediately regardless of this setting.</p>
     */
    private Integer maxRetries = 2;

    /**
     * Semantic version string of the current prompt template.
     *
     * <p>Stored on every {@code ResumeAnalysis} entity. Increment this
     * value whenever the system or user prompt template changes to enable
     * before/after comparison and A/B testing.</p>
     */
    private String promptVersion = "1.0.0";
}
