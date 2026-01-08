package com.scisbo.filesapi.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.scisbo.filesapi.builder.AccessTokenRequestBuilder;
import com.scisbo.filesapi.builder.FileDataBuilder;
import com.scisbo.filesapi.builder.FileUploadRequestBuilder;
import com.scisbo.filesapi.config.FilesApiConfig;
import com.scisbo.filesapi.data.file.FileInfo;
import com.scisbo.filesapi.data.file.FileType;
import com.scisbo.filesapi.data.request.AccessTokenRequest;
import com.scisbo.filesapi.data.request.FileData;
import com.scisbo.filesapi.data.request.FileUploadRequest;
import com.scisbo.filesapi.data.response.AccessTokenResponse;
import com.scisbo.filesapi.data.response.FileUploadResponse;
import com.scisbo.filesapi.exception.AccessTokenException;
import com.scisbo.filesapi.exception.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Production-ready Files API client with proper error handling, logging, and async support
 */
public class FilesApiClient {
    
    private static final Logger logger = Logger.getLogger(FilesApiClient.class.getName());
    
    private final FilesApiConfig config;
    private final HttpClient httpClient;
    private final Gson gson;
    
    public FilesApiClient(FilesApiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getConnectionTimeout())
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Uploads files synchronously
     */
    public FileUploadResponse uploadFiles(FileUploadRequest request) throws FileUploadException {
        try {
            if (config.isLoggingEnabled()) {
                logger.info("Starting file upload for " + request.getFiles().size() + " files");
            }
            
            MultipartBodyPublisher bodyPublisher = new MultipartBodyPublisher();
            
            // Add files to multipart body
            for (FileData file : request.getFiles()) {
                bodyPublisher.addFile("files", file);
            }
            
            // Add metadata
            for (Map.Entry<String, String> entry : request.getMetadata().entrySet()) {
                bodyPublisher.addPart(entry.getKey(), entry.getValue());
            }
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "?path=" + request.getPath()))
                    .header("Content-Type", "multipart/form-data; boundary=" + bodyPublisher.getBoundary())
                    .header("API-KEY", config.getApiKey())
                    .timeout(config.getReadTimeout())
                    .POST(bodyPublisher.build())
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (config.isLoggingEnabled()) {
                logger.info("Upload response status: " + response.statusCode());
            }
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseUploadResponse(response.body());
            } else {
                throw new FileUploadException(
                    "File upload failed with status: " + response.statusCode(),
                    response.statusCode(),
                    response.body()
                );
            }
            
        } catch (IOException | InterruptedException e) {
            throw new FileUploadException("Failed to upload files", e);
        }
    }
    
    /**
     * Uploads files asynchronously
     */
    public CompletableFuture<FileUploadResponse> uploadFilesAsync(FileUploadRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return uploadFiles(request);
            } catch (FileUploadException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Legacy method for uploading MultipartFile objects
     */
    public void uploadMultipartFiles(List<MultipartFile> files, Consumer<List<FileInfo>> onFinish) 
            throws FileUploadException {
        uploadMultipartFiles("", files, null, onFinish);
    }
    
    /**
     * Legacy method for uploading MultipartFile objects with path and metadata
     */
    public void uploadMultipartFiles(String path, List<MultipartFile> files, 
                                   Map<String, String> metadata, Consumer<List<FileInfo>> onFinish) 
            throws FileUploadException {
        
        if (files == null || files.isEmpty()) {
            throw new FileUploadException("File list is empty");
        }
        
        try {
            List<FileData> fileDataList = new ArrayList<>();
            for (MultipartFile file : files) {
                FileData fileData = FileDataBuilder.builder()
                        .filename(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .content(file.getInputStream())
                        .build();
                fileDataList.add(fileData);
            }
            
            FileUploadRequest request = FileUploadRequestBuilder.builder()
                    .path(path)
                    .addFiles(fileDataList)
                    .addMetadata(metadata)
                    .build();
            
            FileUploadResponse response = uploadFiles(request);
            
            if (response.isSuccess() && !response.getUploadedFiles().isEmpty()) {
                onFinish.accept(response.getUploadedFiles());
            }
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to process multipart files", e);
        }
    }
    
    /**
     * Requests an access token for a file
     */
    public AccessTokenResponse requestAccessToken(String fileId) throws AccessTokenException {
        return requestAccessToken(List.of(fileId));
    }

    /**
     * Requests an access token for a file
     */
    public AccessTokenResponse requestAccessToken(String fileId, Duration duration) throws AccessTokenException {
        return requestAccessToken(List.of(fileId), null, duration);
    }
    
    /**
     * Requests an access token for multiple files
     */
    public AccessTokenResponse requestAccessToken(List<String> fileIds) throws AccessTokenException {
        return requestAccessToken(fileIds, null, Duration.ofMinutes(15));
    }

    public AccessTokenResponse requestAccessToken(List<String> fileIds, Duration duration) throws AccessTokenException {
        return requestAccessToken(fileIds, null, duration);
    }
    
    /**
     * Requests an access token for multiple files with optional userId
     */
    public AccessTokenResponse requestAccessToken(List<String> fileIds, String userId, Duration duration) throws AccessTokenException {
        try {
            if (config.isLoggingEnabled()) {
                logger.info("Requesting access token for " + fileIds.size() + " files");
            }
            
            AccessTokenRequest request = AccessTokenRequestBuilder.builder()
                    .addFileIds(fileIds)
                    .userId(userId)
                    .duration(duration)
                    .build();
            
            String requestBody = gson.toJson(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getAccessTokenUrl()))
                    .header("Content-Type", "application/json")
                    .header("API-KEY", config.getApiKey())
                    .timeout(config.getReadTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (config.isLoggingEnabled()) {
                logger.info("Access token response status: " + response.statusCode());
            }
            
            if (response.statusCode() == 200) {
                return parseAccessTokenResponse(response.body());
            } else {
                throw new AccessTokenException(
                    "Access token request failed with status: " + response.statusCode(),
                    response.statusCode(),
                    response.body()
                );
            }
            
        } catch (IOException | InterruptedException e) {
            throw new AccessTokenException("Failed to request access token", e);
        }
    }
    
    /**
     * Requests an access token asynchronously
     */
    public CompletableFuture<AccessTokenResponse> requestAccessTokenAsync(List<String> fileIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return requestAccessToken(fileIds);
            } catch (AccessTokenException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Generates a preview URL for a file
     */
    public String generatePreviewUrl(String fileId, String accessToken) {
        return String.format("%s/%s/download?access_token=%s", 
                config.getBaseUrl(), fileId, accessToken);
    }
    
    private FileUploadResponse parseUploadResponse(String responseBody) throws FileUploadException {
        try {
            JsonObject responseData = gson.fromJson(responseBody, JsonObject.class);
            
            if (responseData.has("data") && !responseData.get("data").isJsonNull()) {
                responseData = responseData.get("data").getAsJsonObject();
                
                if (responseData.has("uploadedFiles")) {
                    JsonArray uploadedFiles = responseData.get("uploadedFiles").getAsJsonArray();
                    List<FileInfo> fileInfos = new ArrayList<>();
                    
                    uploadedFiles.forEach(fileElement -> {
                        JsonObject fileObj = fileElement.getAsJsonObject();
                        JsonObject fileMetadata = fileObj.get("fileMetadata").getAsJsonObject();
                        JsonObject fileType = fileMetadata.get("fileType").getAsJsonObject();
                        
                        FileInfo fileInfo = new FileInfo(
                                fileMetadata.get("id").getAsString(),
                                fileMetadata.get("storedFilename").getAsString(),
                                fileMetadata.get("originalFilename").getAsString(),
                                new FileType(
                                        fileType.get("mimeType").getAsString(),
                                        fileType.get("extension").getAsString()
                                ),
                                new HashMap<>(),
                                LocalDateTime.parse(fileMetadata.get("createdAt").getAsString())
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                        );
                        
                        fileInfos.add(fileInfo);
                    });
                    
                    return new FileUploadResponse(true, "Upload successful", fileInfos);
                }
            }
            
            return new FileUploadResponse(false, "No files uploaded", new ArrayList<>());
            
        } catch (Exception e) {
            throw new FileUploadException("Failed to parse upload response", e);
        }
    }
    
    private AccessTokenResponse parseAccessTokenResponse(String responseBody) throws AccessTokenException {
        try {
            JsonObject data = gson.fromJson(responseBody, JsonObject.class);
            
            if (data.has("data") && !data.get("data").isJsonNull()) {
                JsonObject dataObj = data.get("data").getAsJsonObject();
                String token = dataObj.get("token").getAsString();
                return new AccessTokenResponse(true, "Token generated successfully", token);
            }
            
            return new AccessTokenResponse(false, "No token generated", null);
            
        } catch (Exception e) {
            throw new AccessTokenException("Failed to parse access token response", e);
        }
    }
    
    /**
     * Helper class for building multipart form data
     */
    private static class MultipartBodyPublisher {
        private static final String BOUNDARY = "JavaBoundary" + System.currentTimeMillis();
        private final StringBuilder body = new StringBuilder();
        private final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        
        public MultipartBodyPublisher addFile(String fieldName, FileData file) throws IOException {
            String fileName = file.getFilename();
            String contentType = file.getContentType();
            
            // Prepare the header for the file part
            body.append("--").append(BOUNDARY).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"").append(fieldName)
                    .append("\"; filename=\"").append(fileName).append("\"\r\n")
                    .append("Content-Type: ").append(contentType != null ? contentType : "application/octet-stream")
                    .append("\r\n\r\n");
            
            // Write the header to the output stream
            outputStream.write(body.toString().getBytes());
            
            // Write the file content to the output stream
            file.getContent().transferTo(outputStream);
            
            // Write the line break after the file content
            outputStream.write("\r\n".getBytes());
            
            // Clear the StringBuilder for the next file
            body.setLength(0);
            
            return this;
        }
        
        public MultipartBodyPublisher addPart(String fieldName, String value) throws IOException {
            body.append("--").append(BOUNDARY).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"\r\n")
                    .append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n")
                    .append(value).append("\r\n");
            
            outputStream.write(body.toString().getBytes());
            body.setLength(0); // Clear the StringBuilder for the next part
            
            return this;
        }
        
        public HttpRequest.BodyPublisher build() {
            try {
                outputStream.write(("--" + BOUNDARY + "--\r\n").getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error finishing multipart body", e);
            }
            return HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray());
        }
        
        public String getBoundary() {
            return BOUNDARY;
        }
    }
}
