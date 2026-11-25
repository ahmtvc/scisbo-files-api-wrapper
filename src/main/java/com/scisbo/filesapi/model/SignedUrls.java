package com.scisbo.filesapi.model;

/** A rendition that was never generated is given the original URL, so every field is usable. */
public record SignedUrls(String original, String thumbnail, String medium) {
}
