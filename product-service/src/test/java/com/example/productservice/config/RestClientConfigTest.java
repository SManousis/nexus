package com.example.productservice.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

class RestClientConfigTest {
    @Test
    void configuresBoundedConnectAndReadTimeouts() {
        SimpleClientHttpRequestFactory requestFactory = mock(SimpleClientHttpRequestFactory.class);
        new RestClientConfig().createRestTemplate(requestFactory);
        verify(requestFactory).setConnectTimeout(2_000);
        verify(requestFactory).setReadTimeout(3_000);
    }
}
