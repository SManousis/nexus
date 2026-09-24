package com.example.productservice.client;

import com.example.productservice.config.AppProperties;
import com.example.productservice.exception.InvalidMediaReferenceException;
import com.example.productservice.exception.MediaServiceUnavailableException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.Accessors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class MediaOwnershipClient {

    private final RestTemplate restTemplate;
    private final String mediaServiceBaseUrl;

    public MediaOwnershipClient(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.mediaServiceBaseUrl = appProperties.media().baseUrl();
    }

    public void verifyOwnedImage(String imageId, String sellerId, String bearerToken) {
        MediaMetadataResponse metadata;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            metadata =
                    restTemplate
                            .exchange(
                                    mediaServiceBaseUrl + "/media/images/{id}/metadata",
                                    HttpMethod.GET,
                                    new HttpEntity<>(headers),
                                    MediaMetadataResponse.class,
                                    imageId)
                            .getBody();
        } catch (RestClientResponseException exception) {
            int status = exception.getRawStatusCode();
            if (status == 404) {
                throw new InvalidMediaReferenceException("Image " + imageId + " was not found");
            }
            if (status == 403) {
                throw new AccessDeniedException("You do not own this media asset");
            }
            throw new MediaServiceUnavailableException(
                    "Media validation is temporarily unavailable", exception);
        } catch (RestClientException | IllegalStateException exception) {
            throw new MediaServiceUnavailableException(
                    "Media validation is temporarily unavailable", exception);
        }

        if (metadata == null || metadata.id() == null || !imageId.equals(metadata.id())) {
            throw new InvalidMediaReferenceException("Image " + imageId + " was not found");
        }
        if (!sellerId.equals(metadata.sellerId())) {
            throw new AccessDeniedException("You do not own this media asset");
        }
        if (metadata.contentType() == null || !metadata.contentType().startsWith("image/")) {
            throw new InvalidMediaReferenceException("Media " + imageId + " is not an image");
        }
    }

    @Value
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class MediaMetadataResponse {
        String id;
        String sellerId;
        String contentType;
        long sizeBytes;

        @JsonCreator
        public MediaMetadataResponse(
                @JsonProperty("id") String id,
                @JsonProperty("sellerId") String sellerId,
                @JsonProperty("contentType") String contentType,
                @JsonProperty("sizeBytes") long sizeBytes) {
            this.id = id;
            this.sellerId = sellerId;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
        }
    }
}
