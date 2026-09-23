package com.example.userservice.dto;

import com.example.userservice.model.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

/** Safe user view — password is never included. */
@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UserProfileResponse {
    String id;
    String username;
    String email;
    String role;
    String avatarMediaId;
    Instant createdAt;

    @JsonCreator
    public UserProfileResponse(
            @JsonProperty("id") String id,
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("role") String role,
            @JsonProperty("avatarMediaId") String avatarMediaId,
            @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatarMediaId = avatarMediaId;
        this.createdAt = createdAt;
    }

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getAvatarMediaId(),
                user.getCreatedAt());
    }
}
