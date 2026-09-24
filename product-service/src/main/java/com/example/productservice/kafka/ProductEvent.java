package com.example.productservice.kafka;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProductEvent {
    // PRODUCT_CREATED | PRODUCT_UPDATED | PRODUCT_DELETED
    String eventType;
    // Kafka partition key → ordering per product
    String productId;
    String sellerId;
    String name;
    BigDecimal price;
    Integer stock;

    @JsonAlias("imageUrls")
    List<String> imageIds;

    Instant occurredAt;

    @JsonCreator
    public ProductEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("productId") String productId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("name") String name,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") Integer stock,
            @JsonProperty("imageIds") List<String> imageIds,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventType = eventType;
        this.productId = productId;
        this.sellerId = sellerId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageIds = imageIds;
        this.occurredAt = occurredAt;
    }
}
