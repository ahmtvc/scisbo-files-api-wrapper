package com.scisbo.filesapi.builder;

import com.scisbo.filesapi.data.request.AccessTokenRequest;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating AccessTokenRequest objects
 */
public class AccessTokenRequestBuilder {
    
    private final List<String> fileIds = new ArrayList<>();
    private String userId;

    private Duration duration = Duration.ofMinutes(15);
    
    private AccessTokenRequestBuilder() {}
    
    /**
     * Creates a new builder instance
     */
    public static AccessTokenRequestBuilder builder() {
        return new AccessTokenRequestBuilder();
    }
    
    /**
     * Adds a file ID to the request
     */
    public AccessTokenRequestBuilder addFileId(String fileId) {
        if (fileId != null && !fileId.trim().isEmpty()) {
            this.fileIds.add(fileId);
        }
        return this;
    }
    
    /**
     * Adds multiple file IDs to the request
     */
    public AccessTokenRequestBuilder addFileIds(List<String> fileIds) {
        if (fileIds != null) {
            for (String fileId : fileIds) {
                if (fileId != null && !fileId.trim().isEmpty()) {
                    this.fileIds.add(fileId);
                }
            }
        }
        return this;
    }
    
    /**
     * Sets the user ID for the request
     */
    public AccessTokenRequestBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public AccessTokenRequestBuilder duration(Duration duration) {
        this.duration = duration;
        return this;
    }
    
    /**
     * Builds the AccessTokenRequest
     */
    public AccessTokenRequest build() {
        if (fileIds.isEmpty()) {
            throw new IllegalStateException("At least one file ID must be added to the access token request");
        }
        return new AccessTokenRequest(new ArrayList<>(fileIds), userId, this.duration.toMillis());
    }
}