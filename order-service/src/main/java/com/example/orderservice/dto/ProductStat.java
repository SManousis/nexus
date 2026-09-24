package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * One product's totals across a user's non-cancelled orders. {@code name} and {@code imageId} come
 * from the most recent order snapshot, so a product that was renamed or deleted after purchase is
 * still shown.
 */
@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProductStat {
    String productId;
    String name;
    String imageId;
    int quantity;
    BigDecimal amount;

    @JsonCreator
    public ProductStat(
            @JsonProperty("productId") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("imageId") String imageId,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("amount") BigDecimal amount) {
        this.productId = productId;
        this.name = name;
        this.imageId = imageId;
        this.quantity = quantity;
        this.amount = amount;
    }
}
