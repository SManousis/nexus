package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OrderItem {
    String productId;
    String name;
    BigDecimal unitPrice;
    int quantity;
    String imageId;

    @JsonCreator
    public OrderItem(
            @JsonProperty("productId") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("unitPrice") BigDecimal unitPrice,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("imageId") String imageId) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageId = imageId;
    }
}
