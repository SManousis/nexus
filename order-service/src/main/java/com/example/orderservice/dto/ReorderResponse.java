package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ReorderResponse {
    CartResponse cart;
    List<UnavailableItem> unavailableItems;

    @JsonCreator
    public ReorderResponse(
            @JsonProperty("cart") CartResponse cart,
            @JsonProperty("unavailableItems") List<UnavailableItem> unavailableItems) {
        this.cart = cart;
        this.unavailableItems = unavailableItems;
    }
}
