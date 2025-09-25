package com.scisbo.filesapi.builder;

import com.scisbo.filesapi.data.request.FileData;
import com.scisbo.filesapi.data.request.FileUploadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating FileUploadRequest objects
 */
public class FileUploadRequestBuilder {
    
    private String path = "";
    private final List<FileData> files = new ArrayList<>();
    private final Map<String, String> metadata = new HashMap<>();
    
    private FileUploadRequestBuilder() {}
    
    /**
     * Creates a new builder instance
     */
    public static FileUploadRequestBuilder builder() {
        return new FileUploadRequestBuilder();
    }
    
    /**
     * Sets the upload path
     */
    public FileUploadRequestBuilder path(String path) {
        this.path = path != null ? path : "";
        return this;
    }
    
    /**
     * Adds a file to the upload request
     */
    public FileUploadRequestBuilder addFile(FileData file) {
        if (file != null) {
            this.files.add(file);
        }
        return this;
    }
    
    /**
     * Adds multiple files to the upload request
     */
    public FileUploadRequestBuilder addFiles(List<FileData> files) {
        if (files != null) {
            this.files.addAll(files);
        }
        return this;
    }
    
    /**
     * Adds metadata to the upload request
     */
    public FileUploadRequestBuilder addMetadata(String key, String value) {
        if (key != null && value != null) {
            this.metadata.put(key, value);
        }
        return this;
    }
    
    /**
     * Adds multiple metadata entries to the upload request
     */
    public FileUploadRequestBuilder addMetadata(Map<String, String> metadata) {
        if (metadata != null) {
            this.metadata.putAll(metadata);
        }
        return this;
    }
    
    /**
     * Builds the FileUploadRequest
     */
    public FileUploadRequest build() {
        if (files.isEmpty()) {
            throw new IllegalStateException("At least one file must be added to the upload request");
        }
        return new FileUploadRequest(path, new ArrayList<>(files), new HashMap<>(metadata));
    }
}
