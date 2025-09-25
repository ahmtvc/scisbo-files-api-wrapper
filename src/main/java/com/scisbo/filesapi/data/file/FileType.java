package com.scisbo.filesapi.data.file;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Represents file type information including MIME type and extension
 */
@Data
@AllArgsConstructor
public class FileType {
    
    @SerializedName("mimeType")
    @NonNull
    private String mimeType;
    
    @SerializedName("extension")
    @NonNull
    private String extension;
}
