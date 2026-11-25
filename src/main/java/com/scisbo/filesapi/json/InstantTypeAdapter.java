package com.scisbo.filesapi.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

/** The files service writes timestamps as ISO-8601 instants, e.g. {@code 2026-11-25T10:00:00.123Z}. */
public class InstantTypeAdapter extends TypeAdapter<Instant> {

    @Override
    public void write(JsonWriter writer, Instant value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }

        writer.value(value.toString());
    }

    @Override
    public Instant read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        return Instant.parse(reader.nextString());
    }
}
