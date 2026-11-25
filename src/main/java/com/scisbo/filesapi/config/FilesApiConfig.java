package com.scisbo.filesapi.config;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;

/**
 * {@code baseUrl} is the files service root, e.g. {@code http://files-service} or
 * {@code https://host/api-gateway/api/v1/files}; a trailing slash is ignored.
 */
@Getter
@Builder
public class FilesApiConfig {

    @NonNull
    private final String apiKey;

    @NonNull
    private final String baseUrl;

    @Builder.Default
    private final Duration connectTimeout = Duration.ofSeconds(10);

    @Builder.Default
    private final Duration requestTimeout = Duration.ofSeconds(30);

    /** Sending bytes takes longer than an API call. */
    @Builder.Default
    private final Duration uploadTimeout = Duration.ofMinutes(5);

    public String url(String path) {
        String root = this.baseUrl.endsWith("/") ? this.baseUrl.substring(0, this.baseUrl.length() - 1) : this.baseUrl;
        return root + path;
    }
}
