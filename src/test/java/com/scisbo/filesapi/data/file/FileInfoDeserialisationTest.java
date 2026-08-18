package com.scisbo.filesapi.data.file;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The file service serialises its timestamps from {@code LocalDateTime}, so they arrive as ISO-8601
 * strings rather than numbers. Deserialising straight into a {@code long} used to throw.
 */
class FileInfoDeserialisationTest {

    private final Gson gson = new Gson();

    /** Shaped exactly like a document from {@code GET /files?ids=}. */
    private static final String FILE_SERVICE_PAYLOAD = """
            {
              "id": "77ea4f2f-2f55-41d4-b4f1-b2705efb6d4f",
              "originalFilename": "my holiday photo (1).png",
              "storedFilename": "77ea4f2f_my holiday photo (1).png",
              "contentType": "image/png",
              "fileType": { "mimeType": "image/png", "extension": ".png" },
              "size": 113687,
              "width": 800,
              "height": 600,
              "blurhash": "LDHV9Z3B|cO?z4NKo1aOhVf%fQf%",
              "variants": {
                "thumbnail": {
                  "storedFilename": "77ea4f2f_thumbnail.webp",
                  "mimeType": "image/webp",
                  "size": 862,
                  "width": 256,
                  "height": 192
                }
              },
              "createdAt": "2026-08-18T16:59:59.892"
            }
            """;

    @Test
    void readsAFileServiceDocument() {
        FileInfo info = this.gson.fromJson(FILE_SERVICE_PAYLOAD, FileInfo.class);

        assertEquals("my holiday photo (1).png", info.getOriginalFilename());
        assertEquals("image/png", info.getFileType().getMimeType());
        assertEquals(113687, info.getSize());
        assertEquals(800, info.getWidth());
        assertEquals("LDHV9Z3B|cO?z4NKo1aOhVf%fQf%", info.getBlurhash());
        assertTrue(info.hasVariant("thumbnail"));
        assertEquals(256, info.getVariants().get("thumbnail").getWidth());
    }

    @Test
    void readsAnIsoTimestampIntoEpochMillis() {
        FileInfo info = this.gson.fromJson(FILE_SERVICE_PAYLOAD, FileInfo.class);

        long expected = LocalDateTime.parse("2026-08-18T16:59:59.892")
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        assertEquals(expected, info.getCreatedAt());
        assertEquals(expected, info.getCreatedAtInstant().toEpochMilli());
    }

    @Test
    void stillReadsEpochMillisSentAsANumber() {
        FileInfo info = this.gson.fromJson("{\"id\":\"a\",\"createdAt\":1787068311258}", FileInfo.class);

        assertEquals(1787068311258L, info.getCreatedAt());
    }

    @Test
    void toleratesAMissingOrNullTimestamp() {
        assertEquals(0L, this.gson.fromJson("{\"id\":\"a\"}", FileInfo.class).getCreatedAt());
        assertEquals(0L, this.gson.fromJson("{\"id\":\"a\",\"createdAt\":null}", FileInfo.class).getCreatedAt());
    }

    @Test
    void leavesVariantsAbsentForAFileThatHasNone() {
        FileInfo info = this.gson.fromJson("{\"id\":\"a\",\"createdAt\":\"2026-08-18T16:59:59.892\"}", FileInfo.class);

        assertNull(info.getVariants());
        assertFalse(info.hasVariant("thumbnail"));
    }
}
