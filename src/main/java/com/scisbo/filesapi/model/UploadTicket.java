package com.scisbo.filesapi.model;

import java.time.Instant;
import java.util.Map;

/**
 * Where and how to send the bytes. Hand it to the browser as is: the URL is the credential, it can
 * be retried until {@code expiresAt}, and only one upload completes.
 */
public record UploadTicket(
        String objectId,
        String bucket,
        String key,
        String uploadUrl,
        String method,
        Map<String, String> headers,
        Instant expiresAt
) {
}
