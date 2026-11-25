package com.scisbo.filesapi.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.scisbo.filesapi.config.FilesApiConfig;
import com.scisbo.filesapi.exception.FilesApiException;
import com.scisbo.filesapi.json.InstantTypeAdapter;
import com.scisbo.filesapi.model.FileObject;
import com.scisbo.filesapi.model.SignedUrls;
import com.scisbo.filesapi.model.UploadRequest;
import com.scisbo.filesapi.model.UploadTicket;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Client for the files service. Thread safe; create one per service and reuse it.
 * <p>
 * Every JSON response is wrapped as {@code {success, message, data}}; the methods here return the
 * unwrapped {@code data}.
 */
public class FilesApiClient {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON = "application/json";

    private static final Type OBJECT_LIST = new TypeToken<List<FileObject>>() { }.getType();
    private static final Type SIGNED_URLS_RESPONSE = new TypeToken<SignedUrlsResponse>() { }.getType();

    private final FilesApiConfig config;
    private final HttpClient httpClient;
    private final Gson gson;

    public FilesApiClient(FilesApiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(config.getConnectTimeout()).build();
        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantTypeAdapter()).create();
    }

    /** Reserves an object and returns where to send its bytes; no bytes move yet. */
    public UploadTicket createUpload(UploadRequest request) throws FilesApiException {
        HttpRequest httpRequest = this.apiRequest("/uploads")
                .header(CONTENT_TYPE_HEADER, JSON)
                .POST(HttpRequest.BodyPublishers.ofString(this.gson.toJson(request)))
                .build();

        return this.send(httpRequest, UploadTicket.class);
    }

    /**
     * Sends the bytes for a ticket. Only for a service that already holds the content; a browser
     * should be handed the ticket so the bytes never pass through the calling service.
     */
    public FileObject upload(UploadTicket ticket, InputStream content, long size) throws FilesApiException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(ticket.uploadUrl()))
                .timeout(this.config.getUploadTimeout())
                .method(ticket.method(), HttpRequest.BodyPublishers.fromPublisher(
                        HttpRequest.BodyPublishers.ofInputStream(() -> content), size));

        if (ticket.headers() != null) {
            ticket.headers().forEach(builder::header);
        }

        return this.send(builder.build(), FileObject.class);
    }

    public FileObject uploadDirect(UploadRequest request, InputStream content) throws FilesApiException {
        return this.upload(this.createUpload(request), content, request.size());
    }

    /** Ids that do not exist, are not stored yet or are not readable by this client are left out. */
    public List<FileObject> getObjects(Collection<String> objectIds) throws FilesApiException {
        if (objectIds == null || objectIds.isEmpty()) {
            return List.of();
        }

        HttpRequest httpRequest = this.apiRequest("/objects?" + idsQuery(objectIds)).GET().build();
        List<FileObject> objects = this.send(httpRequest, OBJECT_LIST);

        return objects == null ? List.of() : objects;
    }

    public Optional<FileObject> getObject(String objectId) throws FilesApiException {
        HttpRequest httpRequest = this.apiRequest("/objects/" + encode(objectId)).GET().build();

        try {
            return Optional.ofNullable(this.send(httpRequest, FileObject.class));
        } catch (FilesApiException exception) {
            if (exception.isNotFound()) {
                return Optional.empty();
            }

            throw exception;
        }
    }

    /**
     * Download URLs for every readable id, keyed by id, all valid for at least {@code ttl}. Public
     * objects get their permanent URL.
     */
    public Map<String, SignedUrls> signDownloadUrls(Collection<String> objectIds, Duration ttl) throws FilesApiException {
        if (objectIds == null || objectIds.isEmpty()) {
            return Map.of();
        }

        JsonObject body = new JsonObject();
        body.add("ids", this.gson.toJsonTree(List.copyOf(objectIds)));

        if (ttl != null) {
            body.addProperty("ttlSeconds", ttl.getSeconds());
        }

        HttpRequest httpRequest = this.apiRequest("/objects/signed-urls")
                .header(CONTENT_TYPE_HEADER, JSON)
                .POST(HttpRequest.BodyPublishers.ofString(this.gson.toJson(body)))
                .build();

        SignedUrlsResponse response = this.send(httpRequest, SIGNED_URLS_RESPONSE);

        return response == null || response.urls() == null ? Map.of() : response.urls();
    }

    /** Deleting something that is already gone is not an error. */
    public void deleteObject(String objectId) throws FilesApiException {
        HttpRequest httpRequest = this.apiRequest("/objects/" + encode(objectId)).DELETE().build();

        try {
            this.send(httpRequest, Void.class);
        } catch (FilesApiException exception) {
            if (!exception.isNotFound()) {
                throw exception;
            }
        }
    }

    /** The permanent URL of a public object; {@code variant} may be null for the original. */
    public String publicUrl(String objectId, String variant) {
        String url = this.config.url("/o/" + encode(objectId));

        return variant == null || variant.isBlank() ? url : url + "?variant=" + encode(variant);
    }

    private HttpRequest.Builder apiRequest(String path) {
        return HttpRequest.newBuilder(URI.create(this.config.url(path)))
                .timeout(this.config.getRequestTimeout())
                .header(API_KEY_HEADER, this.config.getApiKey());
    }

    private <T> T send(HttpRequest request, Type dataType) throws FilesApiException {
        HttpResponse<String> response;

        try {
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new FilesApiException("Could not reach the files service", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FilesApiException("Interrupted while calling the files service", exception);
        }

        JsonObject envelope = this.parseEnvelope(response.body());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new FilesApiException(this.messageOf(envelope, response), response.statusCode());
        }

        if (envelope == null || !envelope.has("data") || envelope.get("data").isJsonNull()) {
            return null;
        }

        try {
            return this.gson.fromJson(envelope.get("data"), dataType);
        } catch (JsonParseException exception) {
            throw new FilesApiException("The files service sent a response that could not be read", exception);
        }
    }

    private JsonObject parseEnvelope(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            JsonElement element = this.gson.fromJson(body, JsonElement.class);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (JsonParseException notJson) {
            return null;
        }
    }

    private String messageOf(JsonObject envelope, HttpResponse<String> response) {
        if (envelope != null && envelope.has("message") && !envelope.get("message").isJsonNull()) {
            return envelope.get("message").getAsString();
        }

        return "The files service answered " + response.request().method() + " " + response.uri().getPath()
                + " with status " + response.statusCode();
    }

    private static String idsQuery(Collection<String> objectIds) {
        return objectIds.stream().map(objectId -> "ids=" + encode(objectId)).collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private record SignedUrlsResponse(Map<String, SignedUrls> urls, Instant expiresAt) {
    }
}
