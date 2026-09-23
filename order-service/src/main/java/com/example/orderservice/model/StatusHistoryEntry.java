package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class StatusHistoryEntry {
    OrderStatus status;
    Instant changedAt;
    String changedBy;

    @JsonCreator
    public StatusHistoryEntry(
            @JsonProperty("status") OrderStatus status,
            @JsonProperty("changedAt") Instant changedAt,
            @JsonProperty("changedBy") String changedBy) {
        this.status = status;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }
}
