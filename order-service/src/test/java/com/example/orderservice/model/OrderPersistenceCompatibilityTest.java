package com.example.orderservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderservice.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

class OrderPersistenceCompatibilityTest {
    @Test
    void roundTripsImmutableOrderValuesThroughMongoAndJson() throws Exception {
        MongoCustomConversions conversions = new MongoCustomConversions(List.of());
        MongoMappingContext context = new MongoMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();
        MappingMongoConverter converter =
                new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        converter.afterPropertiesSet();

        Order order = new Order();
        order.setBuyerId("buyer-1");
        order.setSellerId("seller-1");
        order.setItems(
                List.of(
                        new OrderItem(
                                "product-1", "Product", new BigDecimal("12.50"), 2, "image-1")));
        order.setShippingAddress(new ShippingAddress("Street 1", "Athens", "12345", "GR"));
        order.setStatus(OrderStatus.PENDING);
        order.setStatusHistory(
                List.of(
                        new StatusHistoryEntry(
                                OrderStatus.PENDING,
                                Instant.parse("2026-01-01T00:00:00Z"),
                                "buyer-1")));
        Document document = new Document();
        converter.write(order, document);
        Order restored = converter.read(Order.class, document);
        assertThat(restored.getItems()).isEqualTo(order.getItems());
        assertThat(restored.getShippingAddress()).isEqualTo(order.getShippingAddress());
        assertThat(restored.getStatusHistory()).isEqualTo(order.getStatusHistory());

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        OrderResponse response = OrderResponse.from(restored);
        assertThat(mapper.readValue(mapper.writeValueAsBytes(response), OrderResponse.class))
                .isEqualTo(response);

        Cart cart = new Cart("buyer-1");
        cart.setItems(
                List.of(
                        new CartItem(
                                "product-1",
                                "seller-1",
                                "Product",
                                2,
                                new BigDecimal("12.50"),
                                Instant.parse("2026-01-01T00:00:00Z"))));
        Document cartDocument = new Document();
        converter.write(cart, cartDocument);
        assertThat(converter.read(Cart.class, cartDocument).getItems()).isEqualTo(cart.getItems());
    }
}
