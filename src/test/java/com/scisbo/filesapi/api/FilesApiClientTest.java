package com.scisbo.filesapi.api;

import com.scisbo.filesapi.config.FilesApiConfig;
import com.scisbo.filesapi.exception.FilesApiException;
import com.scisbo.filesapi.model.FileObject;
import com.scisbo.filesapi.model.SignedUrls;
import com.scisbo.filesapi.model.UploadRequest;
import com.scisbo.filesapi.model.UploadTicket;
import com.scisbo.filesapi.model.Visibility;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesApiClientTest {

    private static final String API_KEY = "test-key";

    private static final String OBJECT_JSON = """
            {"id":"o1","bucket":"chat","key":"g1/messages/a.png","name":"a.png","contentType":"image/png",
             "size":42,"visibility":"PRIVATE","ownerId":"u1","status":"AVAILABLE",
             "variants":{"thumbnail":{"contentType":"image/webp","size":7,"width":256,"height":192}},
             "createdAt":"2026-11-25T10:00:00.123Z","modifiedAt":"2026-11-25T10:00:01Z"}""";

    private final Map<String, RecordedRequest> requestByPath = new ConcurrentHashMap<>();

    private HttpServer server;
    private FilesApiClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.baseUrl = "http://127.0.0.1:" + this.server.getAddress().getPort() + "/files/";

        this.respond("/files/uploads", 201, """
                {"success":true,"data":{"objectId":"o1","bucket":"chat","key":"g1/messages/a.png",
                 "uploadUrl":"%s/o/o1?expires=1&signature=s","method":"PUT",
                 "headers":{"Content-Type":"image/png"},"expiresAt":"2026-11-25T10:15:00Z"}}"""
                .formatted(this.baseUrl.substring(0, this.baseUrl.length() - 1)));
        this.respond("/files/o/o1", 201, "{\"success\":true,\"data\":" + OBJECT_JSON + "}");
        this.respond("/files/objects", 200, "{\"success\":true,\"data\":[" + OBJECT_JSON + "]}");
        this.respond("/files/objects/missing", 404, "{\"success\":false,\"message\":\"Object not found\"}");
        this.respond("/files/objects/forbidden", 403, "{\"success\":false,\"message\":\"You may not store objects in bucket 'drive'\"}");
        this.respond("/files/objects/signed-urls", 200, """
                {"success":true,"data":{"urls":{"o1":{"original":"u","thumbnail":"t","medium":"u"}},
                 "expiresAt":"2026-11-25T12:00:00Z"}}""");

        this.server.start();

        this.client = new FilesApiClient(FilesApiConfig.builder().apiKey(API_KEY).baseUrl(this.baseUrl).build());
    }

    @AfterEach
    void tearDown() {
        this.server.stop(0);
    }

    @Test
    void reservesAnUploadAndSendsTheBytesToTheTicketsUrlWithoutTheApiKey() throws FilesApiException {
        byte[] content = "png bytes".getBytes(StandardCharsets.UTF_8);
        UploadRequest request = UploadRequest.builder()
                .bucket("chat").path("g1/messages").filename("a.png").contentType("image/png")
                .size(content.length).visibility(Visibility.PRIVATE).ownerId("u1")
                .build();

        FileObject uploaded = this.client.uploadDirect(request, new ByteArrayInputStream(content));

        RecordedRequest reservation = this.requestByPath.get("/files/uploads");
        assertEquals("POST", reservation.method());
        assertEquals(API_KEY, reservation.apiKey());
        assertTrue(reservation.body().contains("\"bucket\":\"chat\""));
        assertTrue(reservation.body().contains("\"visibility\":\"PRIVATE\""));

        RecordedRequest upload = this.requestByPath.get("/files/o/o1");
        assertEquals("PUT", upload.method());
        assertNull(upload.apiKey());
        assertEquals("image/png", upload.contentType());
        assertEquals("png bytes", upload.body());

        assertEquals("o1", uploaded.id());
    }

    @Test
    void readsObjectsWithTheirTimestampsAndVariants() throws FilesApiException {
        List<FileObject> objects = this.client.getObjects(List.of("o1", "a b"));

        assertEquals("ids=o1&ids=a%20b", this.requestByPath.get("/files/objects").query());

        FileObject object = objects.get(0);
        assertEquals(Instant.parse("2026-11-25T10:00:00.123Z"), object.createdAt());
        assertEquals(256, object.variants().get("thumbnail").width());
        assertTrue(object.isAvailable());
        assertTrue(object.isImage());
        assertTrue(object.isInFolder("g1"));
        assertNull(object.width());
    }

    @Test
    void reportsAMissingObjectAsEmptyAndOtherFailuresWithTheServicesMessage() throws FilesApiException {
        assertEquals(Optional.empty(), this.client.getObject("missing"));

        this.client.deleteObject("missing");

        FilesApiException exception = assertThrows(FilesApiException.class, () -> this.client.getObject("forbidden"));
        assertEquals(403, exception.getStatusCode());
        assertEquals("You may not store objects in bucket 'drive'", exception.getMessage());
    }

    @Test
    void signsUrlsForABatch() throws FilesApiException {
        Map<String, SignedUrls> urls = this.client.signDownloadUrls(List.of("o1"), Duration.ofMinutes(30));

        assertEquals("t", urls.get("o1").thumbnail());
        assertTrue(this.requestByPath.get("/files/objects/signed-urls").body().contains("\"ttlSeconds\":1800"));
    }

    @Test
    void buildsPublicUrlsWithoutACall() {
        assertEquals(this.baseUrl + "o/o1?variant=thumbnail", this.client.publicUrl("o1", "thumbnail"));
        assertEquals(this.baseUrl + "o/o1", this.client.publicUrl("o1", null));
    }

    private void respond(String path, int status, String body) {
        this.server.createContext(path, exchange -> {
            if (!exchange.getRequestURI().getPath().equals(path)) {
                this.reply(exchange, 404, "{\"success\":false,\"message\":\"Not found\"}");
                return;
            }

            this.requestByPath.put(path, RecordedRequest.of(exchange));
            this.reply(exchange, status, body);
        });
    }

    private void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private record RecordedRequest(String method, String apiKey, String contentType, String query, String body) {

        static RecordedRequest of(HttpExchange exchange) throws IOException {
            return new RecordedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestHeaders().getFirst("X-Api-Key"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestURI().getRawQuery(),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
