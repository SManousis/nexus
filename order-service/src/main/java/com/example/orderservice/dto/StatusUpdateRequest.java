package com.example.orderservice.dto;

import com.example.orderservice.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class StatusUpdateRequest {
    @NotNull OrderStatus status;

    @JsonCreator
    public StatusUpdateRequest(@JsonProperty("status") OrderStatus status) {
        this.status = status;
    }
}
