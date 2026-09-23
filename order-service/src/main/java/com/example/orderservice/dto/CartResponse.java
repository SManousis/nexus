package com.example.orderservice.dto;

import com.example.orderservice.model.Cart;
import com.example.orderservice.model.CartItem;
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
public class CartResponse {
    String id;
    List<CartItem> items;
    BigDecimal subtotal;
    Instant updatedAt;

    @JsonCreator
    public CartResponse(
            @JsonProperty("id") String id,
            @JsonProperty("items") List<CartItem> items,
            @JsonProperty("subtotal") BigDecimal subtotal,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.id = id;
        this.items = items;
        this.subtotal = subtotal;
        this.updatedAt = updatedAt;
    }

    public static CartResponse from(Cart cart) {
        BigDecimal subtotal =
                cart.getItems().stream()
                        .map(
                                item ->
                                        item.unitPriceSnapshot()
                                                .multiply(BigDecimal.valueOf(item.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(
                cart.getId(), List.copyOf(cart.getItems()), subtotal, cart.getUpdatedAt());
    }
}
