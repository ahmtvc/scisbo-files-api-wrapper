# Files API Wrapper - API Documentation

## Overview

The Files API Wrapper provides a production-ready Java client for interacting with file upload and access token services. It includes comprehensive error handling, builder patterns, async support, and extensive configuration options.

## Usage with Maven and JitPack


To use the Files API Wrapper with Maven and JitPack, follow these steps:

1. Add JitPack repository to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the Files API Wrapper dependency to your `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.ahmtvc</groupId>
        <artifactId>scisbo-files-api-wrapper</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

3. Add the following code to your `pom.xml`:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Usage Examples

### Basic File Upload

```java
// Configure client
FilesApiConfig config = FilesApiConfig.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.example.com/")
    .build();

FilesApiClient client = new FilesApiClient(config);

// Create file data
FileData fileData = FileDataBuilder.builder()
    .filename("document.pdf")
    .contentType("application/pdf")
    .content(inputStream)
    .build();

// Create upload request
FileUploadRequest request = FileUploadRequestBuilder.builder()
    .path("/documents")
    .addFile(fileData)
    .addMetadata("category", "important")
    .build();

// Upload file
try {
    FileUploadResponse response = client.uploadFiles(request);
    if (response.isSuccess()) {
        System.out.println("Upload successful!");
        response.getUploadedFiles().forEach(file -> {
            System.out.println("File ID: " + file.getId());
        });
    }
} catch (FileUploadException e) {
    System.err.println("Upload failed: " + e.getMessage());
}
```

### Async File Upload

```java
CompletableFuture<FileUploadResponse> future = client.uploadFilesAsync(request);
future.thenAccept(response -> {
    if (response.isSuccess()) {
        System.out.println("Async upload completed!");
    }
}).exceptionally(throwable -> {
    System.err.println("Async upload failed: " + throwable.getMessage());
    return null;
});
```

### Access Token Request

```java
try {
    AccessTokenResponse response = client.requestAccessToken("file-id-123");
    if (response.isSuccess()) {
        String token = response.getToken();
        String previewUrl = client.generatePreviewUrl("file-id-123", token);
        System.out.println("Preview URL: " + previewUrl);
    }
} catch (AccessTokenException e) {
    System.err.println("Token request failed: " + e.getMessage());
}
```

### Presigned Upload (preferred)

Rather than streaming bytes through your service, ask the files service to authorise the upload
and let the client send the bytes straight to it.

```java
// 1. Authorise the upload. The declared type and size are checked before a token is issued.
UploadIntentResponse intent = client.requestUploadIntent(
        "chatapp/group-1/messages",   // path
        "holiday.jpg",                // filename
        "image/jpeg",                 // declared content type
        file.getSize(),               // declared size in bytes
        Map.of("ACCESS", "PUBLIC"),   // metadata
        userId);                      // owner, when uploading for one of your users

// 2. Hand intent.getUploadUrl() and intent.getFileId() to the browser, which PUTs the bytes
//    itself. A server side caller can send them here instead:
UploadCommitResponse committed = client.uploadBytesToIntent(intent, inputStream, "image/jpeg");
```

The token is single use and expires (10 minutes by default). When the bytes arrive their real type
is detected from the content and compared with what was declared, so an upload cannot turn into a
different kind of file than the one that was authorised.

`uploadFileDirect(...)` does both steps in one call for callers that already hold the content.

### Image Variants and Blurhash

Uploaded images are processed asynchronously into smaller renditions. Ask for one with the
`variant` parameter:

```java
String thumbnail = client.generateDownloadUrl(fileId, token, FilesApiClient.VARIANT_THUMBNAIL);
String medium    = client.generateDownloadUrl(fileId, token, FilesApiClient.VARIANT_MEDIUM);
String original  = client.generateDownloadUrl(fileId, token, null);
```

Requesting a variant that does not exist - a non-image, an image still being processed, or anything
uploaded before variants existed - serves the original rather than failing, so it is always safe to
ask for a thumbnail.

`FileInfo` carries `width`, `height`, `blurhash` and `variants` once processing has finished; they
are null until then, so treat them as optional.

### Batch Metadata Lookup

```java
List<FileInfo> files = client.getFiles(List.of(fileIdA, fileIdB, fileIdC));
```

## Core Components

### 1. FilesApiClient

The main client class for interacting with the Files API.

```java
FilesApiClient client = new FilesApiClient(config);
```

#### Methods

##### File Upload Operations

- `uploadFiles(FileUploadRequest request)` - Synchronous file upload
- `uploadFilesAsync(FileUploadRequest request)` - Asynchronous file upload
- `uploadMultipartFiles(List<MultipartFile> files, Consumer<List<FileInfo>> onFinish)` - Legacy Spring integration
- `uploadMultipartFiles(String path, List<MultipartFile> files, Map<String, String> metadata, Consumer<List<FileInfo>> onFinish)` - Legacy with path and metadata

##### Presigned Upload Operations

- `requestUploadIntent(String path, String filename, String contentType, long size)` - Authorise an upload
- `requestUploadIntent(String path, String filename, String contentType, long size, Map<String, String> metadata, String onBehalfOfUserId)` - Authorise an upload for one of your users
- `uploadBytesToIntent(UploadIntentResponse intent, InputStream content, String contentType)` - Send the bytes for an intent
- `uploadFileDirect(...)` - Request an intent and send the bytes in one call

##### Metadata Operations

- `getFiles(List<String> fileIds)` - Look several files up at once

##### Access Token Operations

- `requestAccessToken(String fileId)` - Request token for single file
- `requestAccessToken(List<String> fileIds)` - Request token for multiple files
- `requestAccessTokenAsync(List<String> fileIds)` - Asynchronous token request
- `generatePreviewUrl(String fileId, String accessToken)` - Generate file preview URL
- `generateDownloadUrl(String fileId, String accessToken, String variant)` - Generate a URL for a specific size (`thumbnail`, `medium`, or null for the original)

### 2. FilesApiConfig

Configuration class for the API client.

```java
FilesApiConfig config = FilesApiConfig.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.example.com")
    .connectionTimeout(Duration.ofSeconds(30))
    .readTimeout(Duration.ofSeconds(60))
    .maxRetries(3)
    .enableLogging(true)
    .build();
```

#### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `apiKey` | String | Required | API key for authentication |
| `baseUrl` | String | Required | Base URL for the API (upload endpoint: `/api/files`, access token endpoint: `/api/access-tokens`) |
| `connectionTimeout` | Duration | 30 seconds | HTTP connection timeout |
| `readTimeout` | Duration | 60 seconds | HTTP read timeout |
| `maxRetries` | int | 3 | Maximum retry attempts |
| `enableLogging` | boolean | true | Enable/disable logging |

### 3. Data Models

#### FileInfo

Represents uploaded file information.

```java
public class FileInfo {
    private String id;                    // Unique file identifier
    private String storedFilename;        // Server-side filename
    private String originalFilename;      // Original client filename
    private FileType fileType;           // MIME type and extension
    private Map<String, String> metadata; // File metadata
    private long createdAt;              // Creation timestamp
}
```

#### FileType

Represents file type information.

```java
public class FileType {
    private String mimeType;    // MIME type (e.g., "application/pdf")
    private String extension;   // File extension (e.g., "pdf")
}
```

#### FileData

Represents file data for upload requests.

```java
public class FileData {
    private String filename;     // File name
    private String contentType;  // MIME type
    private InputStream content; // File content stream
}
```

### 4. Request Models

#### FileUploadRequest

Request model for file upload operations.

```java
public class FileUploadRequest {
    private String path;                    // Upload path
    private List<FileData> files;          // Files to upload
    private Map<String, String> metadata;  // File metadata
}
```

#### AccessTokenRequest

Request model for access token generation.

```java
public class AccessTokenRequest {
    private List<String> fileIds;  // File IDs to request tokens for
    private String userId;         // Optional user ID for the request
}
```

### 5. Response Models

#### FileUploadResponse

Response model for file upload operations.

```java
public class FileUploadResponse {
    private boolean success;              // Upload success status
    private String message;               // Response message
    private List<FileInfo> uploadedFiles; // Uploaded file information
}
```

#### AccessTokenResponse

Response model for access token generation.

```java
public class AccessTokenResponse {
    private boolean success;  // Request success status
    private String message;   // Response message
    private String token;     // Generated access token
}
```

### 6. Builder Classes

#### FileUploadRequestBuilder

Fluent builder for creating file upload requests.

```java
FileUploadRequest request = FileUploadRequestBuilder.builder()
    .path("/documents")
    .addFile(fileData)
    .addMetadata("category", "important")
    .build();
```

#### FileDataBuilder

Fluent builder for creating file data objects.

```java
FileData fileData = FileDataBuilder.builder()
    .filename("document.pdf")
    .contentType("application/pdf")
    .content(inputStream)
    .build();
```

#### AccessTokenRequestBuilder

Fluent builder for creating access token requests.

```java
AccessTokenRequest request = AccessTokenRequestBuilder.builder()
    .addFileId("file-id-1")
    .addFileId("file-id-2")
    .build();
```

### 7. Exception Handling

#### FilesApiException

Base exception for all API operations.

```java
public class FilesApiException extends Exception {
    private int statusCode;     // HTTP status code
    private String responseBody; // Response body
}
```

#### FileUploadException

Exception thrown when file upload operations fail.

```java
public class FileUploadException extends FilesApiException {
    // Inherits from FilesApiException
}
```

#### AccessTokenException

Exception thrown when access token operations fail.

```java
public class AccessTokenException extends FilesApiException {
    // Inherits from FilesApiException
}
```

## Error Handling Best Practices

1. **Always handle exceptions**: Wrap API calls in try-catch blocks
2. **Check response status**: Verify `isSuccess()` before processing results
3. **Log errors**: Use the status code and response body for debugging
4. **Implement retry logic**: Use the `maxRetries` configuration for transient failures

```java
try {
    FileUploadResponse response = client.uploadFiles(request);
    if (response.isSuccess()) {
        // Process successful upload
    } else {
        // Handle API-level errors
        System.err.println("Upload failed: " + response.getMessage());
    }
} catch (FileUploadException e) {
    // Handle network/parsing errors
    System.err.println("Upload error: " + e.getMessage());
    if (e.getStatusCode() != -1) {
        System.err.println("HTTP Status: " + e.getStatusCode());
        System.err.println("Response: " + e.getResponseBody());
    }
}
```

## Performance Considerations

1. **Use async operations** for better throughput
2. **Configure appropriate timeouts** based on file sizes
3. **Enable logging** only in development environments
4. **Reuse client instances** to avoid connection overhead
5. **Batch file uploads** when possible

## Security Considerations

1. **Store API keys securely** (environment variables, secure vaults)
2. **Validate file types** before upload
3. **Implement file size limits**
4. **Use HTTPS** for all API communications
5. **Sanitize metadata** to prevent injection attacks




