package com.scisbo.filesapi.data.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirmation that an upload's bytes were accepted and the file now exists.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadCommitResponse {

    @SerializedName("fileId")
    private String fileId;

    @SerializedName("originalFilename")
    private String originalFilename;

    @SerializedName("size")
    private long size;

    @SerializedName("contentType")
    private String contentType;

    @SerializedName("downloadUrl")
    private String downloadUrl;

    /**
     * True when the file is an image whose variants and blurhash are still being generated. They
     * arrive later; until then the original is served for every variant.
     */
    @SerializedName("processing")
    private boolean processing;
}
