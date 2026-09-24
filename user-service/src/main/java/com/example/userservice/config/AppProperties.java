package com.example.userservice.config;

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

/**
 * Typed, validated binding of all `app.*` properties.
 *
 * <p>Fail-fast on startup: if JWT_SECRET is blank or expiration is negative, the application
 * refuses to start rather than silently misbehaving.
 */
@Validated
@ConfigurationProperties(prefix = "app")
@Value
@Accessors(fluent = true)
public class AppProperties {
    @Valid JwtProperties jwt;
    @Valid CorsProperties cors;
    @Valid MediaProperties media;

    @ConstructorBinding
    public AppProperties(JwtProperties jwt, CorsProperties cors, MediaProperties media) {
        this.jwt = jwt;
        this.cors = cors;
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

        @NotBlank(message = "app.jwt.issuer must not be blank")
        String issuer;

        @NotBlank(message = "app.jwt.audience must not be blank")
        String audience;

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
    public static class MediaProperties {
        @NotBlank(message = "app.media.service-name must not be blank")
        String serviceName;

        @ConstructorBinding
        public MediaProperties(String serviceName) {
            this.serviceName = serviceName;
        }
    }
}
