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
public class CheckoutResponse {
    String checkoutGroupId;
    List<OrderResponse> orders;

    @JsonCreator
    public CheckoutResponse(
            @JsonProperty("checkoutGroupId") String checkoutGroupId,
            @JsonProperty("orders") List<OrderResponse> orders) {
        this.checkoutGroupId = checkoutGroupId;
        this.orders = orders;
    }
}
