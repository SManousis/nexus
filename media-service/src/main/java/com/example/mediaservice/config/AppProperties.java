package com.example.mediaservice.config;

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
    @Valid StorageProperties storage;
    @Valid KafkaProperties kafka;

    @ConstructorBinding
    public AppProperties(
            JwtProperties jwt,
            CorsProperties cors,
            StorageProperties storage,
            KafkaProperties kafka) {
        this.jwt = jwt;
        this.cors = cors;
        this.storage = storage;
        this.kafka = kafka;
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
    public static class StorageProperties {
        @NotBlank(message = "app.storage.base-path must not be blank")
        String basePath;

        @NotBlank(message = "app.storage.public-base-url must not be blank")
        String publicBaseUrl;

        @ConstructorBinding
        public StorageProperties(String basePath, String publicBaseUrl) {
            this.basePath = basePath;
            this.publicBaseUrl = publicBaseUrl;
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
            @NotBlank String imageUploaded;
            @NotBlank String imageDeleted;
            @NotBlank String productDeleted;

            @ConstructorBinding
            public Topics(String imageUploaded, String imageDeleted, String productDeleted) {
                this.imageUploaded = imageUploaded;
                this.imageDeleted = imageDeleted;
                this.productDeleted = productDeleted;
            }
        }
    }
}
