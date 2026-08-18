package com.scisbo.filesapi.data.file;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generated rendition of an image ({@code thumbnail} or {@code medium}).
 * <p>
 * There is no URL here on purpose: renditions are fetched through the normal download endpoint
 * with {@code ?variant=}, so the caller builds the URL with whatever access token it holds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileVariantInfo {

    @SerializedName("storedFilename")
    private String storedFilename;

    @SerializedName("mimeType")
    private String mimeType;

    @SerializedName("size")
    private long size;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;
}
