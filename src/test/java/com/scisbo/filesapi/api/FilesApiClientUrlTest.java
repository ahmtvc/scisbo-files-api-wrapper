package com.scisbo.filesapi.api;

import com.scisbo.filesapi.config.FilesApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The URL shapes here are a contract with the files service and with every frontend that builds
 * the same URLs itself, so they are pinned rather than left to be inferred.
 */
class FilesApiClientUrlTest {

    private static final String BASE_URL = "https://files.example.com/apis/file-system";

    private FilesApiClient client;
    private FilesApiConfig config;

    @BeforeEach
    void setUp() {
        this.config = FilesApiConfig.builder()
                .apiKey("test-key")
                .baseUrl(BASE_URL)
                .build();

        this.client = new FilesApiClient(this.config);
    }

    @Test
    void buildsAnOriginalDownloadUrlWithAToken() {
        assertEquals(BASE_URL + "/abc/download?access_token=tok",
                this.client.generateDownloadUrl("abc", "tok", null));
    }

    @Test
    void buildsAVariantDownloadUrlWithAToken() {
        assertEquals(BASE_URL + "/abc/download?access_token=tok&variant=thumbnail",
                this.client.generateDownloadUrl("abc", "tok", FilesApiClient.VARIANT_THUMBNAIL));
    }

    @Test
    void omitsTheTokenForPubliclyReadableFiles() {
        assertEquals(BASE_URL + "/abc/download?variant=medium",
                this.client.generateDownloadUrl("abc", null, FilesApiClient.VARIANT_MEDIUM));
    }

    @Test
    void treatsOriginalAsNoVariantSoTheUrlStaysTheOneOlderClientsUse() {
        assertEquals(BASE_URL + "/abc/download?access_token=tok",
                this.client.generateDownloadUrl("abc", "tok", FilesApiClient.VARIANT_ORIGINAL));
    }

    @Test
    void escapesTokensThatContainUrlSyntax() {
        String url = this.client.generateDownloadUrl("abc", "a+b/c=d", null);

        assertTrue(url.contains("access_token=a%2Bb%2Fc%3Dd"), url);
    }

    @Test
    void resolvesTheRelativeUploadUrlAgainstTheConfiguredBase() {
        assertEquals(BASE_URL + "/uploads/tok123", this.config.getUploadUrl("/uploads/tok123"));
        assertEquals(BASE_URL + "/uploads/tok123", this.config.getUploadUrl("uploads/tok123"));
    }

    @Test
    void leavesAnAbsoluteUploadUrlAlone() {
        assertEquals("https://other.example.com/uploads/t",
                this.config.getUploadUrl("https://other.example.com/uploads/t"));
    }

    @Test
    void rejectsAnIntentWithNoUploadUrl() {
        assertThrows(IllegalArgumentException.class, () -> this.config.getUploadUrl(null));
        assertThrows(IllegalArgumentException.class, () -> this.config.getUploadUrl("  "));
    }

    @Test
    void pointsTheIntentAndBatchEndpointsAtTheRightPaths() {
        assertEquals(BASE_URL + "/uploads/intent", this.config.getUploadIntentUrl());
        assertEquals(BASE_URL + "/files", this.config.getBatchMetadataUrl());
        assertEquals(BASE_URL + "/access-tokens", this.config.getAccessTokenUrl());
    }
}
