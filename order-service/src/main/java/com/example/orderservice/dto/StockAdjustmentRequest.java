package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class StockAdjustmentRequest {
    int delta;

    @JsonCreator
    public StockAdjustmentRequest(@JsonProperty("delta") int delta) {
        this.delta = delta;
    }
}
