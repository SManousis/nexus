package com.example.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    RestTemplate mediaRestTemplate() {
        return createRestTemplate(new SimpleClientHttpRequestFactory());
    }

    RestTemplate createRestTemplate(SimpleClientHttpRequestFactory requestFactory) {
        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(3_000);
        return new RestTemplate(requestFactory);
    }
}
