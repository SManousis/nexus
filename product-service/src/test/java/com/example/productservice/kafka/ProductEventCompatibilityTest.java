package com.example.productservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ProductEventCompatibilityTest {

    @Test
    void readsLegacyImageUrlsFromQueuedProductEvents() throws Exception {
        ProductEvent event = new ObjectMapper().readValue("{\n  \"eventType\": \"PRODUCT_DELETED\",\n  \"productId\": \"product-1\",\n  \"sellerId\": \"seller-1\",\n  \"imageUrls\": [\"media-1\"]\n}\n", ProductEvent.class);

        assertThat(event.imageIds()).containsExactly("media-1");
    }
}
