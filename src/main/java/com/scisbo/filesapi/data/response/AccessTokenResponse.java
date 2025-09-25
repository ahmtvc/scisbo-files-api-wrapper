package com.scisbo.filesapi.data.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response model for access token generation
 */
@Data
@AllArgsConstructor
public class AccessTokenResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("token")
    private String token;
}
