package com.example.mediaservice.kafka;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

// Mirrors product-service ProductEvent — only fields needed for image cleanup
@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProductDeletedEvent {
    String eventType;
    String productId;
    String sellerId;

    @JsonAlias("imageUrls")
    List<String> imageIds;

    Instant occurredAt;

    @JsonCreator
    public ProductDeletedEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("productId") String productId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("imageIds") List<String> imageIds,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventType = eventType;
        this.productId = productId;
        this.sellerId = sellerId;
        this.imageIds = imageIds;
        this.occurredAt = occurredAt;
    }
}
