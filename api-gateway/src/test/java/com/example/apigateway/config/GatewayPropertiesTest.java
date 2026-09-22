package com.example.apigateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.util.unit.DataSize;

import com.example.apigateway.config.GatewayProperties.CorsProperties;
import com.example.apigateway.config.GatewayProperties.JwtProperties;
import com.example.apigateway.config.GatewayProperties.UploadProperties;

class GatewayPropertiesTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-characters-long";

    @Test
    void bindsExternalConfigurationToNestedProperties() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "app.jwt.secret", SECRET,
                "app.jwt.issuer", "user-service",
                "app.jwt.audience", "buy-01-api",
                "app.cors.allowed-origins[0]", "https://shop.example",
                "app.cors.allowed-origins[1]", "https://admin.example",
                "app.upload.max-request-size", "12MB")));

        GatewayProperties properties = binder.bind("app", Bindable.of(GatewayProperties.class)).get();

        assertThat(properties.jwt().secret()).isEqualTo(SECRET);
        assertThat(properties.jwt().issuer()).isEqualTo("user-service");
        assertThat(properties.jwt().audience()).isEqualTo("buy-01-api");
        assertThat(properties.cors().allowedOrigins())
                .containsExactly("https://shop.example", "https://admin.example");
        assertThat(properties.upload().maxRequestSize()).isEqualTo(DataSize.ofMegabytes(12));
    }

    @Test
    void jwtValueEqualityIncludesEverySetting() {
        JwtProperties value = jwt();
        assertValueContract(value, jwt(),
                new JwtProperties("different-secret", "user-service", "buy-01-api"),
                new JwtProperties(SECRET, "other-issuer", "buy-01-api"),
                new JwtProperties(SECRET, "user-service", "other-audience"),
                new JwtProperties(null, null, null));
        assertThat(value.toString()).contains("issuer=user-service", "audience=buy-01-api");
    }

    @Test
    void corsValueEqualityComparesOriginContents() {
        CorsProperties value = cors();
        assertValueContract(value, cors(),
                new CorsProperties(List.of("https://other.example")),
                new CorsProperties(List.of()), new CorsProperties(null));
        assertThat(value.toString()).contains("https://shop.example");
    }

    @Test
    void uploadValueEqualityComparesSize() {
        UploadProperties value = upload();
        assertValueContract(value, upload(),
                new UploadProperties(DataSize.ofMegabytes(24)), new UploadProperties(null));
        assertThat(value.toString()).contains("maxRequestSize=" + DataSize.ofMegabytes(12));
    }

    @Test
    void gatewayValueEqualityIncludesEveryConfigurationGroup() {
        GatewayProperties value = new GatewayProperties(jwt(), cors(), upload());
        assertValueContract(value, new GatewayProperties(jwt(), cors(), upload()),
                new GatewayProperties(new JwtProperties(SECRET, "other", "buy-01-api"), cors(), upload()),
                new GatewayProperties(jwt(), new CorsProperties(List.of()), upload()),
                new GatewayProperties(jwt(), cors(), new UploadProperties(DataSize.ofMegabytes(24))),
                new GatewayProperties(null, null, null));
        assertThat(value.toString()).contains("GatewayProperties[", "cors=", "upload=");
    }

    private static void assertValueContract(Object value, Object equalValue, Object... differentValues) {
        assertThat(value.equals(value)).isTrue();
        assertThat(value).isEqualTo(equalValue);
        assertThat(equalValue).isEqualTo(value).hasSameHashCodeAs(value);
        assertThat(value.equals(null)).isFalse();
        assertThat(value.equals("unrelated type")).isFalse();
        for (Object different : differentValues) {
            assertThat(value).isNotEqualTo(different);
            assertThat(different).isNotEqualTo(value);
        }
    }

    private static JwtProperties jwt() {
        return new JwtProperties(SECRET, "user-service", "buy-01-api");
    }

    private static CorsProperties cors() {
        return new CorsProperties(List.of("https://shop.example"));
    }

    private static UploadProperties upload() {
        return new UploadProperties(DataSize.ofMegabytes(12));
    }
}
