package com.example.mediaservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ImageEventTest {
    @Test
    void preservesEventPayloadWithoutExposingStorageKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ImageEvent event = new ImageEvent("IMAGE_UPLOADED", "media-1", "seller-1",
                "photo.png", "image/png", 42, Instant.parse("2026-01-01T00:00:00Z"));
        JsonNode payload = mapper.readTree(mapper.writeValueAsBytes(event));
        assertThat(payload.has("storageKey")).isFalse();
        assertThat(payload.get("mediaId").asText()).isEqualTo("media-1");
        assertThat(payload.get("sizeBytes").asLong()).isEqualTo(42);
        assertThat(mapper.treeToValue(payload, ImageEvent.class)).isEqualTo(event);
    }
}
