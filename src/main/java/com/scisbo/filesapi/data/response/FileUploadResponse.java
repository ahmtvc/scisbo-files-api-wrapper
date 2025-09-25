package com.scisbo.filesapi.data.response;

import com.google.gson.annotations.SerializedName;
import com.scisbo.filesapi.data.file.FileInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for file upload operations
 */
@Data
@NoArgsConstructor
public class FileUploadResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("uploadedFiles")
    private List<FileInfo> uploadedFiles;
    
    public FileUploadResponse(boolean success, String message, List<FileInfo> uploadedFiles) {
        this.success = success;
        this.message = message;
        this.uploadedFiles = uploadedFiles != null ? List.copyOf(uploadedFiles) : List.of();
    }
}
