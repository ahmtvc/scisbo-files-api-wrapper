package com.scisbo.filesapi.config;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;

/**
 * Configuration class for the Files API
 */
@Getter
@Builder
public class FilesApiConfig {
    
    @NonNull
    private final String apiKey;
    
    @NonNull
    private final String baseUrl;
    
    @Builder.Default
    private final Duration connectionTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private final Duration readTimeout = Duration.ofSeconds(60);
    
    @Builder.Default
    private final int maxRetries = 3;
    
    @Builder.Default
    private final boolean enableLogging = true;

    public String getAccessTokenUrl() {
        return baseUrl + "/access-tokens";
    }

    public String getUploadIntentUrl() {
        return baseUrl + "/uploads/intent";
    }

    /**
     * Resolves the upload URL returned with an intent. It comes back relative, so it is appended
     * to the configured base rather than used as-is.
     */
    public String getUploadUrl(String relativeUploadUrl) {
        if (relativeUploadUrl == null || relativeUploadUrl.isBlank()) {
            throw new IllegalArgumentException("The upload intent did not include an upload URL");
        }

        if (relativeUploadUrl.startsWith("http://") || relativeUploadUrl.startsWith("https://")) {
            return relativeUploadUrl;
        }

        return baseUrl + (relativeUploadUrl.startsWith("/") ? relativeUploadUrl : "/" + relativeUploadUrl);
    }

    public String getBatchMetadataUrl() {
        return baseUrl + "/files";
    }

    public boolean isLoggingEnabled() {
        return enableLogging;
    }
}
