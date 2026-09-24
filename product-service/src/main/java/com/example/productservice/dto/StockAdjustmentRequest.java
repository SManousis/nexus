package com.example.productservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class StockAdjustmentRequest {
    @NotNull Integer delta;

    @JsonCreator
    public StockAdjustmentRequest(@JsonProperty("delta") Integer delta) {
        this.delta = delta;
    }
}
