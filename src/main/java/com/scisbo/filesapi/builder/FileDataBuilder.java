package com.scisbo.filesapi.builder;

import com.scisbo.filesapi.data.request.FileData;

import java.io.InputStream;

/**
 * Builder for creating FileData objects
 */
public class FileDataBuilder {
    
    private String filename;
    private String contentType;
    private InputStream content;
    
    private FileDataBuilder() {}
    
    /**
     * Creates a new builder instance
     */
    public static FileDataBuilder builder() {
        return new FileDataBuilder();
    }
    
    /**
     * Sets the filename
     */
    public FileDataBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }
    
    /**
     * Sets the content type (MIME type)
     */
    public FileDataBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    /**
     * Sets the file content as InputStream
     */
    public FileDataBuilder content(InputStream content) {
        this.content = content;
        return this;
    }
    
    /**
     * Builds the FileData
     */
    public FileData build() {
        return new FileData(filename, contentType, content);
    }
}
