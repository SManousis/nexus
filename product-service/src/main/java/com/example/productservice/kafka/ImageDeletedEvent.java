package com.example.productservice.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

// Mirrors media-service ImageEvent — only fields needed for cleanup are declared
@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ImageDeletedEvent {
    String eventType;
    String mediaId;
    String sellerId;
    Instant occurredAt;

    @JsonCreator
    public ImageDeletedEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("mediaId") String mediaId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventType = eventType;
        this.mediaId = mediaId;
        this.sellerId = sellerId;
        this.occurredAt = occurredAt;
    }
}
