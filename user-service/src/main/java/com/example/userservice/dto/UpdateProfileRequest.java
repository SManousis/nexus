package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Value
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateProfileRequest {
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    String username;

    /** Avatar Media ID set after uploading via Media Service */
    @Pattern(regexp = ".*\\S.*", message = "Avatar media ID must not be blank")
    String avatarMediaId;

    Boolean removeAvatar;

    @JsonCreator
    public UpdateProfileRequest(
            @JsonProperty("username") String username,
            @JsonProperty("avatarMediaId") String avatarMediaId,
            @JsonProperty("removeAvatar") Boolean removeAvatar) {
        this.username = username;
        this.avatarMediaId = avatarMediaId;
        this.removeAvatar = removeAvatar;
    }
}
