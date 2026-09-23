package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

/** Returned after successful register or login. */
@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AuthResponse {
    String token;
    String userId;
    String username;
    String role;

    @JsonCreator
    public AuthResponse(
            @JsonProperty("token") String token,
            @JsonProperty("userId") String userId,
            @JsonProperty("username") String username,
            @JsonProperty("role") String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
