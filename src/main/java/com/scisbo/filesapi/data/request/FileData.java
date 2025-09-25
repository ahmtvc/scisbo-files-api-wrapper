package com.scisbo.filesapi.data.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.InputStream;

/**
 * Represents file data for upload requests
 */
@Data
@NoArgsConstructor
public class FileData {
    
    @SerializedName("filename")
    @NonNull
    private String filename;
    
    @SerializedName("contentType")
    private String contentType;
    
    @SerializedName("content")
    @NonNull
    private InputStream content;
    
    public FileData(String filename, String contentType, InputStream content) {
        this.filename = filename;
        this.contentType = contentType != null ? contentType : "application/octet-stream";
        this.content = content;
    }
}
