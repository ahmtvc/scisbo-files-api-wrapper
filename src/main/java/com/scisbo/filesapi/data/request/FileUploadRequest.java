package com.scisbo.filesapi.data.request;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Request model for file upload operations
 */
@Data
@NoArgsConstructor
public class FileUploadRequest {
    
    @SerializedName("path")
    private String path;
    
    @SerializedName("files")
    @NonNull
    private List<FileData> files;
    
    @SerializedName("metadata")
    private Map<String, String> metadata;
    
    public FileUploadRequest(String path, List<FileData> files, Map<String, String> metadata) {
        this.path = path != null ? path : "";
        this.files = files;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
}
