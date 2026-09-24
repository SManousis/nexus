package com.example.orderservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

class RestClientConfigTest {
    @Test
    void boundsConnectAndReadTimeouts() {
        HttpComponentsClientHttpRequestFactory factory =
                mock(HttpComponentsClientHttpRequestFactory.class);
        new RestClientConfig().createRestTemplate(factory);
        verify(factory).setConnectTimeout(2_000);
        verify(factory).setReadTimeout(3_000);
    }

    @Test
    void productionTransportSupportsPatchOnJava11() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> method = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext(
                "/stock",
                exchange -> {
                    method.set(exchange.getRequestMethod());
                    body.set(
                            new String(
                                    exchange.getRequestBody().readAllBytes(),
                                    StandardCharsets.UTF_8));
                    byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                });
        server.start();
        try {
            String response =
                    new RestClientConfig()
                            .restTemplate()
                            .patchForObject(
                                    "http://127.0.0.1:" + server.getAddress().getPort() + "/stock",
                                    "adjustment",
                                    String.class);
            assertThat(response).isEqualTo("ok");
            assertThat(method.get()).isEqualTo("PATCH");
            assertThat(body.get()).isEqualTo("adjustment");
        } finally {
            server.stop(0);
        }
    }
}
