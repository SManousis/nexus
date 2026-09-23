package com.example.mediaservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MediaMetadataResponse {
    String id;
    String sellerId;
    String contentType;
    long sizeBytes;

    @JsonCreator
    public MediaMetadataResponse(
            @JsonProperty("id") String id,
            @JsonProperty("sellerId") String sellerId,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("sizeBytes") long sizeBytes) {
        this.id = id;
        this.sellerId = sellerId;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }
}
