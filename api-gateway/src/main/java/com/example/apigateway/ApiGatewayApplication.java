package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import com.example.apigateway.web.MdcContextLifter;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

    static {
        // Registered as early as possible (class-init time, before the Spring context
        // assembles any reactive chains) so it's active for both `main()` and @SpringBootTest.
        MdcContextLifter.register();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

