package com.scisbo.filesapi.exception;

/**
 * Base exception for Files API operations
 */
public class FilesApiException extends Exception {
    
    private final int statusCode;
    private final String responseBody;
    
    public FilesApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public FilesApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public FilesApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public FilesApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (statusCode != -1) {
            sb.append(" [Status: ").append(statusCode).append("]");
        }
        if (responseBody != null) {
            sb.append(" [Response: ").append(responseBody).append("]");
        }
        return sb.toString();
    }
}
