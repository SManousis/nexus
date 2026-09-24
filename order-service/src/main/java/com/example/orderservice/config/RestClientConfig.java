package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    RestTemplate restTemplate() {
        return createRestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    RestTemplate createRestTemplate(HttpComponentsClientHttpRequestFactory requestFactory) {
        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(3_000);
        return new RestTemplate(requestFactory);
    }
}
