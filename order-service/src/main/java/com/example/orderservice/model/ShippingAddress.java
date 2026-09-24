package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ShippingAddress {
    String line1;
    String city;
    String postalCode;
    String country;

    @JsonCreator
    public ShippingAddress(
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
