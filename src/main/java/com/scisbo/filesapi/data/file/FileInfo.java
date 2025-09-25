package com.scisbo.filesapi.data.file;

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
    private long createdAt;
    
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
}
