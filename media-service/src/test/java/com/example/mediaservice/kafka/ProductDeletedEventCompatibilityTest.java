package com.example.mediaservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ProductDeletedEventCompatibilityTest {

    @Test
    void readsLegacyImageUrlsFromQueuedDeletionEvents() throws Exception {
        ProductDeletedEvent event = new ObjectMapper().readValue("{\n  \"eventType\": \"PRODUCT_DELETED\",\n  \"productId\": \"product-1\",\n  \"sellerId\": \"seller-1\",\n  \"imageUrls\": [\"media-1\", \"media-2\"]\n}\n", ProductDeletedEvent.class);

        assertThat(event.imageIds()).containsExactly("media-1", "media-2");
    }
}
