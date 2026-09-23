package com.example.productservice.dto;

import com.example.productservice.model.Product;
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
public class ProductResponse {
    String id;
    String sellerId;
    String name;
    String description;
    BigDecimal price;
    Integer stock;
    List<String> imageIds;
    Instant createdAt;
    Instant updatedAt;

    @JsonCreator
    public ProductResponse(
            @JsonProperty("id") String id,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") Integer stock,
            @JsonProperty("imageIds") List<String> imageIds,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageIds = imageIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageIds(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
