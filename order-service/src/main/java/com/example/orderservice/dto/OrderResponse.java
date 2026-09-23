package com.example.orderservice.dto;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.PaymentMethod;
import com.example.orderservice.model.PaymentStatus;
import com.example.orderservice.model.ShippingAddress;
import com.example.orderservice.model.StatusHistoryEntry;
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
public class OrderResponse {
    String id;
    String checkoutGroupId;
    String buyerId;
    String sellerId;
    List<OrderItem> items;
    BigDecimal subtotal;
    OrderStatus status;
    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    List<StatusHistoryEntry> statusHistory;
    ShippingAddress shippingAddress;
    Instant createdAt;
    Instant updatedAt;

    @JsonCreator
    public OrderResponse(
            @JsonProperty("id") String id,
            @JsonProperty("checkoutGroupId") String checkoutGroupId,
            @JsonProperty("buyerId") String buyerId,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("items") List<OrderItem> items,
            @JsonProperty("subtotal") BigDecimal subtotal,
            @JsonProperty("status") OrderStatus status,
            @JsonProperty("paymentMethod") PaymentMethod paymentMethod,
            @JsonProperty("paymentStatus") PaymentStatus paymentStatus,
            @JsonProperty("statusHistory") List<StatusHistoryEntry> statusHistory,
            @JsonProperty("shippingAddress") ShippingAddress shippingAddress,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.id = id;
        this.checkoutGroupId = checkoutGroupId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.items = items;
        this.subtotal = subtotal;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.statusHistory = statusHistory;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCheckoutGroupId(),
                order.getBuyerId(),
                order.getSellerId(),
                order.getItems(),
                order.getSubtotal(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatusHistory(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
