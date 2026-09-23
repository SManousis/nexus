package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ShippingAddressRequest {
    @NotBlank String line1;
    @NotBlank String city;
    @NotBlank String postalCode;
    @NotBlank String country;

    @JsonCreator
    public ShippingAddressRequest(
            @JsonProperty("line1") String line1,
            @JsonProperty("city") String city,
            @JsonProperty("postalCode") String postalCode,
            @JsonProperty("country") String country) {
        this.line1 = line1;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }
}
