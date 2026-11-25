package com.scisbo.filesapi.model;

import lombok.Builder;

import java.util.Map;

/**
 * Reserves an object. {@code path} is the folder inside the bucket; the service appends the
 * filename and numbers it if the name is taken. {@code visibility} defaults to the bucket's.
 */
@Builder
public record UploadRequest(
        String bucket,
        String path,
        String filename,
        String contentType,
        long size,
        Visibility visibility,
        String ownerId,
        Map<String, String> attributes
) {
}
