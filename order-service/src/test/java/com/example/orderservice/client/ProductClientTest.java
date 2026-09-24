package com.example.orderservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.example.orderservice.config.AppProperties;
import com.example.orderservice.exception.CartConflictException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.exception.ProductServiceUnavailableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class ProductClientTest {
    private MockRestServiceServer server;
    private ProductClient client;

    @BeforeEach
    void setUp() {
        RestTemplate template = new RestTemplate();
        server = MockRestServiceServer.bindTo(template).build();
        client =
                new ProductClient(
                        template,
                        new AppProperties(
                                null,
                                null,
                                new AppProperties.ProductProperties("http://product-service"),
                                null));
    }

    @Test
    void readsProductSnapshotAndForwardsAuthentication() {
        server.expect(requestTo("http://product-service/products/product-1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer buyer-token"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));
        ProductSnapshot product = client.getProduct("product-1", "Bearer buyer-token");
        assertThat(product.id()).isEqualTo("product-1");
        assertThat(product.stock()).isEqualTo(5);
        assertThat(product.imageIds()).isEqualTo(List.of("image-1"));
        server.verify();
    }

    @Test
    void sendsStockAdjustmentAsPatchWithJsonAndAuthentication() {
        server.expect(requestTo("http://product-service/products/product-1/stock"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer buyer-token"))
                .andExpect(content().json("{\"delta\":-2}"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));
        assertThat(client.adjustStock("product-1", -2, "Bearer buyer-token").stock()).isEqualTo(5);
        server.verify();
    }

    @Test
    void mapsStockConflictWithoutLosingItsMeaning() {
        server.expect(requestTo("http://product-service/products/product-1/stock"))
                .andRespond(withStatus(HttpStatus.CONFLICT));
        assertThatThrownBy(() -> client.adjustStock("product-1", -2, "Bearer buyer-token"))
                .isInstanceOf(CartConflictException.class);
        server.verify();
    }

    @Test
    void mapsMissingProduct() {
        server.expect(requestTo("http://product-service/products/missing"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> client.getProduct("missing"))
                .isInstanceOf(NotFoundException.class);
        server.verify();
    }

    @Test
    void mapsUpstreamFailureToRetryableError() {
        server.expect(requestTo("http://product-service/products/product-1"))
                .andRespond(withServerError());
        assertThatThrownBy(() -> client.getProduct("product-1"))
                .isInstanceOf(ProductServiceUnavailableException.class);
        server.verify();
    }

    private String payload() {
        return "{\"id\":\"product-1\",\"sellerId\":\"seller-1\",\"name\":\"Product\","
                + "\"price\":12.50,\"stock\":5,\"imageIds\":[\"image-1\"]}";
    }
}
