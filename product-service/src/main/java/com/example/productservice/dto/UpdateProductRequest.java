package com.example.productservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateProductRequest {
    @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "Name must not be blank")
    @Size(min = 1, max = 120, message = "Name must be 1..120 characters")
    String name;

    @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "Description must not be blank")
    @Size(min = 1, max = 2000, message = "Description must be 1..2000 characters")
    String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price;

    @PositiveOrZero(message = "Stock must be 0 or greater")
    Integer stock;

    @JsonAlias("imageUrls")
    List<String> imageIds;

    @JsonCreator
    public UpdateProductRequest(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") Integer stock,
            @JsonProperty("imageIds") List<String> imageIds) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageIds = imageIds;
    }
}
