package com.scisbo.filesapi.data.file;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.Map;

/**
 * Represents file information returned from the API
 */
@Data
@NoArgsConstructor
public class FileInfo {
    
    @SerializedName("id")
    @NonNull
    private String id;
    
    @SerializedName("storedFilename")
    @NonNull
    private String storedFilename;
    
    @SerializedName("originalFilename")
    @NonNull
    private String originalFilename;
    
    @SerializedName("fileType")
    @NonNull
    private FileType fileType;
    
    @SerializedName("metadata")
    private Map<String, String> metadata;
    
    @SerializedName("createdAt")
    @JsonAdapter(EpochMillisAdapter.class)
    private long createdAt;

    @SerializedName("size")
    private long size;

    /**
     * Pixel dimensions of the original, and the blurhash placeholder.
     * <p>
     * All null for anything that is not an image, and for images whose processing has not finished
     * or was never run (nothing was backfilled when variants were introduced), so callers must
     * treat them as optional.
     */
    @SerializedName("width")
    private Integer width;

    @SerializedName("height")
    private Integer height;

    @SerializedName("blurhash")
    private String blurhash;

    /** Generated renditions keyed by {@code thumbnail} / {@code medium}; empty when there are none. */
    @SerializedName("variants")
    private Map<String, FileVariantInfo> variants;

    public FileInfo(String id, String storedFilename, String originalFilename,
                   FileType fileType, Map<String, String> metadata, long createdAt) {
        this.id = id;
        this.storedFilename = storedFilename;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        this.createdAt = createdAt;
    }

    public Instant getCreatedAtInstant() {
        return Instant.ofEpochMilli(createdAt);
    }

    public boolean hasVariant(String variant) {
        return this.variants != null && this.variants.containsKey(variant);
    }
}
