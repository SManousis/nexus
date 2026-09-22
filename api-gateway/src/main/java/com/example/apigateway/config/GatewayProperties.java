package com.example.apigateway.config;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app")
public final class GatewayProperties {

    private final JwtProperties jwt;
    private final CorsProperties cors;
    private final UploadProperties upload;

    @ConstructorBinding
    public GatewayProperties(@Valid JwtProperties jwt, @Valid CorsProperties cors, @Valid UploadProperties upload) {
        this.jwt = jwt;
        this.cors = cors;
        this.upload = upload;
    }

    public JwtProperties jwt() {
        return jwt;
    }

    public CorsProperties cors() {
        return cors;
    }

    public UploadProperties upload() {
        return upload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GatewayProperties)) return false;
        GatewayProperties that = (GatewayProperties) o;
        return Objects.equals(jwt, that.jwt) && Objects.equals(cors, that.cors) && Objects.equals(upload, that.upload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jwt, cors, upload);
    }

    @Override
    public String toString() {
        return "GatewayProperties[jwt=" + jwt + ", cors=" + cors + ", upload=" + upload + "]";
    }

    public static final class JwtProperties {
        private final String secret;
        private final String issuer;
        private final String audience;

        public JwtProperties(
                @NotBlank(message = "app.jwt.secret must not be blank")
                @Size(min = 32, message = "app.jwt.secret must be at least 32 characters")
                String secret,
                @NotBlank(message = "app.jwt.issuer must not be blank")
                String issuer,
                @NotBlank(message = "app.jwt.audience must not be blank")
                String audience
        ) {
            this.secret = secret;
            this.issuer = issuer;
            this.audience = audience;
        }

        public String secret() {
            return secret;
        }

        public String issuer() {
            return issuer;
        }

        public String audience() {
            return audience;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JwtProperties)) return false;
            JwtProperties that = (JwtProperties) o;
            return Objects.equals(secret, that.secret) && Objects.equals(issuer, that.issuer)
                    && Objects.equals(audience, that.audience);
        }

        @Override
        public int hashCode() {
            return Objects.hash(secret, issuer, audience);
        }

        @Override
        public String toString() {
            return "JwtProperties[secret=" + secret + ", issuer=" + issuer + ", audience=" + audience + "]";
        }
    }

    public static final class CorsProperties {
        private final List<String> allowedOrigins;

        public CorsProperties(
                @NotEmpty(message = "app.cors.allowed-origins must not be empty")
                List<@NotBlank String> allowedOrigins
        ) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> allowedOrigins() {
            return allowedOrigins;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CorsProperties)) return false;
            CorsProperties that = (CorsProperties) o;
            return Objects.equals(allowedOrigins, that.allowedOrigins);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowedOrigins);
        }

        @Override
        public String toString() {
            return "CorsProperties[allowedOrigins=" + allowedOrigins + "]";
        }
    }

    public static final class UploadProperties {
        private final DataSize maxRequestSize;

        public UploadProperties(
                @NotNull(message = "app.upload.max-request-size must not be null")
                DataSize maxRequestSize
        ) {
            this.maxRequestSize = maxRequestSize;
        }

        public DataSize maxRequestSize() {
            return maxRequestSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UploadProperties)) return false;
            UploadProperties that = (UploadProperties) o;
            return Objects.equals(maxRequestSize, that.maxRequestSize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxRequestSize);
        }

        @Override
        public String toString() {
            return "UploadProperties[maxRequestSize=" + maxRequestSize + "]";
        }
    }
}
