package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UnavailableItem {
    String productId;
    String name;
    String reason;

    @JsonCreator
    public UnavailableItem(
            @JsonProperty("productId") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("reason") String reason) {
        this.productId = productId;
        this.name = name;
        this.reason = reason;
    }
}
