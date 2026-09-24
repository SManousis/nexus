package com.example.productservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AppPropertiesBindingTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withInitializer(new ConfigDataApplicationContextInitializer())
                    .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
                    .withUserConfiguration(PropertiesConfiguration.class)
                    .withPropertyValues(
                            "app.jwt.secret=test-secret-that-is-at-least-32-characters-long",
                            "MONGODB_URI=mongodb://localhost:27019/migration-test");

    @Test
    void bindsApplicationYamlAndMongoUri() {
        runner.run(
                context -> {
                    assertThat(context).hasNotFailed();
                    AppProperties properties = context.getBean(AppProperties.class);
                    assertThat(properties.jwt().issuer()).isEqualTo("user-service");
                    assertThat(properties.jwt().audience()).isEqualTo("buy-01-api");
                    assertThat(properties.cors().allowedOrigins())
                            .containsExactly("http://localhost:4200");
                    assertThat(context.getBean(MongoProperties.class).getUri())
                            .isEqualTo("mongodb://localhost:27019/migration-test");
                });
    }

    @Test
    void rejectsAnInvalidJwtSecretAtStartup() {
        runner.withPropertyValues("app.jwt.secret=short")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasStackTraceContaining("jwt.secret");
                        });
    }

    @Test
    void validatesNestedConfigurationAtStartup() {
        runner.withPropertyValues("app.jwt.issuer=")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasStackTraceContaining("jwt.issuer");
                        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({AppProperties.class, MongoProperties.class})
    static class PropertiesConfiguration {}
}
