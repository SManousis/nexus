package com.example.mediaservice.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ImageEvent {
    // IMAGE_UPLOADED | IMAGE_DELETED
    String eventType;
    // used as Kafka partition key → ordering per image
    String mediaId;
    String sellerId;
    String originalFileName;
    String contentType;
    long sizeBytes;
    Instant occurredAt;

    @JsonCreator
    public ImageEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("mediaId") String mediaId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("originalFileName") String originalFileName,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("sizeBytes") long sizeBytes,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventType = eventType;
        this.mediaId = mediaId;
        this.sellerId = sellerId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.occurredAt = occurredAt;
    }
}
