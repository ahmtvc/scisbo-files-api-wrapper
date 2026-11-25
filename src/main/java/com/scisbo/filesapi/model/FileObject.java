package com.scisbo.filesapi.model;

import java.time.Instant;
import java.util.Map;

/**
 * An object as the files service describes it. Image fields stay null until processing finishes
 * (and for anything that is not an image), so treat them as optional. {@code publicUrl} is set only
 * for public objects.
 */
public record FileObject(
        String id,
        String bucket,
        String key,
        String name,
        String contentType,
        String extension,
        long size,
        Visibility visibility,
        String ownerId,
        String status,
        Map<String, String> attributes,
        Integer width,
        Integer height,
        String blurhash,
        String processingStatus,
        Map<String, FileVariant> variants,
        String publicUrl,
        Instant createdAt,
        Instant modifiedAt
) {

    private static final String AVAILABLE = "AVAILABLE";

    public boolean isAvailable() {
        return AVAILABLE.equals(this.status);
    }

    public boolean isImage() {
        return this.contentType != null && this.contentType.startsWith("image/");
    }

    /** Keys use {@code /} between folders, so this matches whole folder names only. */
    public boolean isInFolder(String folderPath) {
        return this.key != null && this.key.startsWith(folderPath.endsWith("/") ? folderPath : folderPath + "/");
    }
}
