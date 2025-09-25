package com.scisbo.filesapi.exception;

/**
 * Exception thrown when access token operations fail
 */
public class AccessTokenException extends FilesApiException {
    
    public AccessTokenException(String message) {
        super(message);
    }
    
    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AccessTokenException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
    
    public AccessTokenException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, statusCode, responseBody, cause);
    }
}
