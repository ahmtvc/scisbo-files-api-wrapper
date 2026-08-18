package com.scisbo.filesapi.data.file;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reads a timestamp that may arrive as epoch millis or as an ISO-8601 local date-time.
 * <p>
 * The file service stores these as {@code LocalDateTime} and serialises them as
 * {@code "2026-08-18T16:59:59.892"}, so a plain {@code long} field fails to parse.
 */
public class EpochMillisAdapter extends TypeAdapter<Long> {

    @Override
    public void write(JsonWriter out, Long value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.longValue());
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) {
            in.nextNull();
            return 0L;
        }

        if (token == JsonToken.NUMBER) {
            return in.nextLong();
        }

        String value = in.nextString();

        if (value == null || value.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException notANumber) {
            return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }
}
