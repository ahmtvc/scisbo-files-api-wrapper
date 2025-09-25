package com.scisbo.filesapi.data.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;
import java.util.List;
import java.util.Objects;

/**
 * Request model for access token generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenRequest {
    
    @SerializedName("fileIds")
    private List<String> fileIds;
    
    @SerializedName("userId")
    private String userId;

    private long duration;
    
    public AccessTokenRequest(List<String> fileIds) {
        this.fileIds = Objects.requireNonNull(fileIds, "File IDs list cannot be null");
        if (fileIds.isEmpty()) {
            throw new IllegalArgumentException("File IDs list cannot be empty");
        }
        this.userId = null;
    }
}
