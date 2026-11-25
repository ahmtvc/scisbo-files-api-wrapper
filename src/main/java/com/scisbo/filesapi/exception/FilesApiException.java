package com.scisbo.filesapi.exception;

/** A call to the files service failed. {@link #getStatusCode()} is -1 when no response arrived. */
public class FilesApiException extends Exception {

    private static final int NO_RESPONSE = -1;

    private final int statusCode;

    public FilesApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FilesApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = NO_RESPONSE;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public boolean isNotFound() {
        return this.statusCode == 404;
    }
}
