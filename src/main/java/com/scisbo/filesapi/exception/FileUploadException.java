package com.scisbo.filesapi.exception;

/**
 * Exception thrown when file upload operations fail
 */
public class FileUploadException extends FilesApiException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileUploadException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
    
    public FileUploadException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, statusCode, responseBody, cause);
    }
}
