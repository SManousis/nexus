package com.example.mediaservice.service;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** Image bytes and media properties established from the content itself. */
@Value
@Accessors(fluent = true)
public class ValidatedImage {
    byte[] content;
    String contentType;
    String extension;

    public ValidatedImage(byte[] content, String contentType, String extension) {
        content = Objects.requireNonNull(content, "content").clone();
        contentType = Objects.requireNonNull(contentType, "contentType");
        extension = Objects.requireNonNull(extension, "extension");
        this.content = content;
        this.contentType = contentType;
        this.extension = extension;
    }

    public byte[] content() {
        return content.clone();
    }
}
