package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CartItem {
    String productId;
    String sellerId;
    String productName;
    int quantity;
    BigDecimal unitPriceSnapshot;
    Instant addedAt;

    @JsonCreator
    public CartItem(
            @JsonProperty("productId") String productId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("productName") String productName,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("unitPriceSnapshot") BigDecimal unitPriceSnapshot,
            @JsonProperty("addedAt") Instant addedAt) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.addedAt = addedAt;
    }
}
