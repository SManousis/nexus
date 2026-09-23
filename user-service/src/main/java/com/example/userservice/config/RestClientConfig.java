package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Primary;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    RestTemplate restTemplate() {
        return createRestTemplate(new SimpleClientHttpRequestFactory());
    }

    RestTemplate createRestTemplate(SimpleClientHttpRequestFactory requestFactory) {
        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(3_000);
        return new RestTemplate(requestFactory);
    }

    @Bean
    @LoadBalanced
    RestTemplate loadBalancedRestTemplate() {
        return createRestTemplate(new SimpleClientHttpRequestFactory());
    }
}
