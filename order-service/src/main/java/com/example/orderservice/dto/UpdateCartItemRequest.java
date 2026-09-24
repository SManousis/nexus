package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateCartItemRequest {
    @Min(0)
    int quantity;

    @JsonCreator
    public UpdateCartItemRequest(@JsonProperty("quantity") int quantity) {
        this.quantity = quantity;
    }
}
