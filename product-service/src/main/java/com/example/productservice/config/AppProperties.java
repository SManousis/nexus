package com.example.productservice.config;

import lombok.Value;
import lombok.experimental.Accessors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app")
@Value
@Accessors(fluent = true)
public class AppProperties {
    @Valid JwtProperties jwt;
    @Valid CorsProperties cors;
    @Valid KafkaProperties kafka;
    @Valid MediaProperties media;

    @ConstructorBinding
    public AppProperties(
            JwtProperties jwt, CorsProperties cors, KafkaProperties kafka, MediaProperties media) {
        this.jwt = jwt;
        this.cors = cors;
        this.kafka = kafka;
        this.media = media;
    }

    @Value
    @Accessors(fluent = true)
    public static class JwtProperties {
        @NotBlank(message = "app.jwt.secret must not be blank")
        @Size(min = 32, message = "app.jwt.secret must be at least 32 characters")
        String secret;

        @Min(value = 60_000, message = "app.jwt.expiration-ms must be at least 60 seconds")
        long expirationMs;

        @NotBlank String issuer;
        @NotBlank String audience;

        @ConstructorBinding
        public JwtProperties(String secret, long expirationMs, String issuer, String audience) {
            this.secret = secret;
            this.expirationMs = expirationMs;
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
    public static class KafkaProperties {
        @Valid Topics topics;

        @ConstructorBinding
        public KafkaProperties(Topics topics) {
            this.topics = topics;
        }

        @Value
        @Accessors(fluent = true)
        public static class Topics {
            @NotBlank String productCreated;
            @NotBlank String productUpdated;
            @NotBlank String productDeleted;
            @NotBlank String imageDeleted;

            @ConstructorBinding
            public Topics(
                    String productCreated,
                    String productUpdated,
                    String productDeleted,
                    String imageDeleted) {
                this.productCreated = productCreated;
                this.productUpdated = productUpdated;
                this.productDeleted = productDeleted;
                this.imageDeleted = imageDeleted;
            }
        }
    }

    @Value
    @Accessors(fluent = true)
    public static class MediaProperties {
        @NotBlank String baseUrl;

        @ConstructorBinding
        public MediaProperties(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
