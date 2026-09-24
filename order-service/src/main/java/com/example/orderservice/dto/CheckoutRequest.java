package com.example.orderservice.dto;

import com.example.orderservice.model.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CheckoutRequest {
    @Valid @NotNull ShippingAddressRequest shippingAddress;
    @NotNull PaymentMethod paymentMethod;

    @JsonCreator
    public CheckoutRequest(
            @JsonProperty("shippingAddress") ShippingAddressRequest shippingAddress,
            @JsonProperty("paymentMethod") PaymentMethod paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }
}
