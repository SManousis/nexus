package com.example.orderservice.config;

import lombok.Value;
import lombok.experimental.Accessors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app")
@Value
@Accessors(fluent = true)
public class AppProperties {
    @Valid JwtProperties jwt;
    @Valid CorsProperties cors;
    @Valid ProductProperties product;
    @Valid KafkaProperties kafka;

    @ConstructorBinding
    public AppProperties(
            JwtProperties jwt,
            CorsProperties cors,
            ProductProperties product,
            KafkaProperties kafka) {
        this.jwt = jwt;
        this.cors = cors;
        this.product = product;
        this.kafka = kafka;
    }

    @Value
    @Accessors(fluent = true)
    public static class JwtProperties {
        @NotBlank
        @Size(min = 32)
        String secret;

        @NotBlank String issuer;
        @NotBlank String audience;

        @ConstructorBinding
        public JwtProperties(String secret, String issuer, String audience) {
            this.secret = secret;
            this.issuer = issuer;
            this.audience = audience;
        }
    }

    @Value
    @Accessors(fluent = true)
    public static class CorsProperties {
        List<@NotBlank String> allowedOrigins;

        @ConstructorBinding
        public CorsProperties(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    @Value
    @Accessors(fluent = true)
    public static class ProductProperties {
        @NotBlank String baseUrl;

        @ConstructorBinding
        public ProductProperties(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    @Value
    @Accessors(fluent = true)
    public static class KafkaProperties {
        @Valid Topics topics;

        @ConstructorBinding
        public KafkaProperties(Topics topics) {
            this.topics = topics;
        }

        @Value
        @Accessors(fluent = true)
        public static class Topics {
            @NotBlank String orderCreated;
            @NotBlank String orderStatusChanged;
            @NotBlank String orderCancelled;

            @ConstructorBinding
            public Topics(String orderCreated, String orderStatusChanged, String orderCancelled) {
                this.orderCreated = orderCreated;
                this.orderStatusChanged = orderStatusChanged;
                this.orderCancelled = orderCancelled;
            }
        }
    }
}
