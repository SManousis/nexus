package com.example.userservice.dto;

import com.example.userservice.model.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SellerSummaryResponse {
    String id;
    String username;

    @JsonCreator
    public SellerSummaryResponse(
            @JsonProperty("id") String id, @JsonProperty("username") String username) {
        this.id = id;
        this.username = username;
    }

    public static SellerSummaryResponse from(User user) {
        return new SellerSummaryResponse(user.getId(), user.getUsername());
    }
}
