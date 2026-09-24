package com.example.orderservice.client;

import com.example.orderservice.config.AppProperties;
import com.example.orderservice.dto.StockAdjustmentRequest;
import com.example.orderservice.exception.CartConflictException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.exception.ProductServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

@Component
public class ProductClient {
    private final RestTemplate client;
    private final String baseUrl;
    public ProductClient(RestTemplate restTemplate, AppProperties properties) {
        this.client = restTemplate;
        this.baseUrl = properties.product().baseUrl();
    }
    public ProductSnapshot getProduct(String productId) {
        return getProduct(productId, null);
    }

    public ProductSnapshot getProduct(String productId, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null) headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            ProductSnapshot product = client.exchange(baseUrl + "/products/{id}", HttpMethod.GET,
                    new HttpEntity<>(headers), ProductSnapshot.class, productId).getBody();
            if (product == null) throw new ProductServiceUnavailableException();
            return product;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NotFoundException("Product not found: " + productId);
        } catch (RestClientException ex) {
            throw new ProductServiceUnavailableException();
        }
    }

    public ProductSnapshot adjustStock(String productId, int delta, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            ProductSnapshot product = client.exchange(baseUrl + "/products/{id}/stock", HttpMethod.PATCH,
                    new HttpEntity<>(new StockAdjustmentRequest(delta), headers),
                    ProductSnapshot.class, productId).getBody();
            if (product == null) throw new ProductServiceUnavailableException();
            return product;
        } catch (HttpClientErrorException.Conflict ex) {
            throw new CartConflictException(
                    "Insufficient stock for product: " + productId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NotFoundException("Product not found: " + productId);
        } catch (RestClientException ex) {
            throw new ProductServiceUnavailableException();
        }
    }
}
