package com.example.orderservice.kafka;

import com.example.orderservice.model.OrderStatus;
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
public class OrderEvent {
    String eventType;
    String orderId;
    String checkoutGroupId;
    String buyerId;
    String sellerId;
    BigDecimal subtotal;
    OrderStatus status;
    Instant occurredAt;

    @JsonCreator
    public OrderEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("checkoutGroupId") String checkoutGroupId,
            @JsonProperty("buyerId") String buyerId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("subtotal") BigDecimal subtotal,
            @JsonProperty("status") OrderStatus status,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventType = eventType;
        this.orderId = orderId;
        this.checkoutGroupId = checkoutGroupId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.subtotal = subtotal;
        this.status = status;
        this.occurredAt = occurredAt;
    }
}
