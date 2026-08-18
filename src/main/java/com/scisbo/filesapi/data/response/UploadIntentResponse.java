package com.scisbo.filesapi.data.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A presigned upload issued by the files service: where to send the bytes, and until when.
 * <p>
 * The upload URL is relative to the API base URL, so it survives the service being reached through
 * different hosts (gateway, reverse proxy) without the client having to rewrite it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadIntentResponse {

    @SerializedName("fileId")
    private String fileId;

    @SerializedName("uploadToken")
    private String uploadToken;

    @SerializedName("uploadUrl")
    private String uploadUrl;

    /** Epoch millis after which the token stops working. */
    @SerializedName("expiresAt")
    private long expiresAt;

    public boolean hasExpired() {
        return System.currentTimeMillis() >= this.expiresAt;
    }
}
