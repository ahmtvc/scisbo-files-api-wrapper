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
    
    public boolean isLoggingEnabled() {
        return enableLogging;
    }
}
