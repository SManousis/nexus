package com.example.userservice.client;

import com.example.userservice.config.AppProperties;
import com.example.userservice.dto.MediaMetadataResponse;
import com.example.userservice.exception.InvalidAvatarMediaException;
import com.example.userservice.exception.MediaServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MediaOwnershipClient {

    private final RestTemplate restTemplate;
    private final String mediaServiceBaseUrl;

    public MediaOwnershipClient(
            @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
            AppProperties properties) {
        this.restTemplate = restTemplate;
        this.mediaServiceBaseUrl = "http://" + properties.media().serviceName();
    }

    public void verifyOwnedImage(String userId, String mediaId, String bearerToken) {
        MediaMetadataResponse metadata;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            metadata = restTemplate.exchange(mediaServiceBaseUrl + "/media/images/{id}/metadata",
                    HttpMethod.GET, new HttpEntity<>(headers), MediaMetadataResponse.class, mediaId).getBody();
        } catch (RestClientResponseException exception) {
            int status = exception.getRawStatusCode();
            if (status == 403 || status == 404) {
                throw new InvalidAvatarMediaException("Invalid avatar media reference");
            }
            throw new MediaServiceUnavailableException(
                    "Media validation is temporarily unavailable", exception);
        } catch (RestClientException | IllegalStateException exception) {
            throw new MediaServiceUnavailableException(
                    "Media validation is temporarily unavailable", exception);
        }

        if (metadata == null
                || !mediaId.equals(metadata.id())
                || !userId.equals(metadata.sellerId())
                || metadata.contentType() == null
                || !metadata.contentType().startsWith("image/")) {
            throw new InvalidAvatarMediaException("Invalid avatar media reference");
        }
    }

    public void deleteOwnedImage(String mediaId, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            restTemplate.exchange(mediaServiceBaseUrl + "/media/images/{id}", HttpMethod.DELETE,
                    new HttpEntity<>(headers), Void.class, mediaId);
        } catch (RestClientResponseException exception) {
            if (exception.getRawStatusCode() == 404) {
                return;
            }
            throw new MediaServiceUnavailableException(
                    "Media cleanup is temporarily unavailable", exception);
        } catch (RestClientException | IllegalStateException exception) {
            throw new MediaServiceUnavailableException(
                    "Media cleanup is temporarily unavailable", exception);
        }
    }
}
