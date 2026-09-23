package com.example.orderservice.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProductSnapshot {
    String id;
    String sellerId;
    String name;
    BigDecimal price;
    Integer stock;
    List<String> imageIds;

    @JsonCreator
    public ProductSnapshot(
            @JsonProperty("id") String id,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("name") String name,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") Integer stock,
            @JsonProperty("imageIds") List<String> imageIds) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageIds = imageIds;
    }

    public ProductSnapshot(
            String id, String sellerId, String name, BigDecimal price, Integer stock) {
        this(id, sellerId, name, price, stock, List.of());
    }
}
