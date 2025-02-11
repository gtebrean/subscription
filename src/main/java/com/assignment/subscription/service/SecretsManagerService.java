package com.assignment.subscription.service;

import com.assignment.subscription.exception.AwsTokenDecodeException;
import com.assignment.subscription.exception.AwsTokenRefreshException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;
    private static final String SECRET_NAME = "api_token"; // AWS Secret Name
    private static final long TOKEN_REFRESH_INTERVAL = 11 * 30 * 24 * 60 * 60L; // 11 months in seconds

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private Instant tokenExpirationTime;


    /**
     * Retrieves `api_token` from AWS Secrets Manager and verifies its expiration.
     */
    public String getApiToken() throws AwsTokenRefreshException {
        if (cachedToken.get() == null || Instant.now().isAfter(tokenExpirationTime)) {
            refreshApiToken();
        }
        return cachedToken.get();
    }

    /**
     * Refreshes the token by fetching it from AWS Secrets Manager and checking expiration.
     */
    @Scheduled(fixedRate = TOKEN_REFRESH_INTERVAL * 1000) // Run every 11 months
    public synchronized void refreshApiToken() throws AwsTokenRefreshException {
        try {
            log.info("Attempting to refresh API token...");

            // Fetch from AWS Secrets Manager
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String apiToken = response.secretString();


            // Update cache only if token is valid


            Instant expiryTime = getTokenExpiry(apiToken);
            cachedToken.set(apiToken);
            tokenExpirationTime = expiryTime != null ? expiryTime : Instant.now().plusSeconds(TOKEN_REFRESH_INTERVAL);

            log.info("API token refreshed successfully.");
        } catch (Exception e) {
            log.error("Failed to refresh API token: {}", e.getMessage());
            throw new AwsTokenRefreshException("Failed to refresh API token");
        }
    }

    /**
     * Decodes the JWT and extracts the expiration time.
     */
    private Instant getTokenExpiry(String jwtToken) throws AwsTokenDecodeException {
        try {
            String[] parts = jwtToken.split("\\."); // JWT format: header.payload.signature
            if (parts.length < 2) {
                return null;
            }

            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            final ObjectMapper objectMapper = new ObjectMapper();
            JsonNode claims = objectMapper.readTree(payloadJson);

            long exp = claims.get("exp").asLong();
            return Instant.ofEpochSecond(exp);
        } catch (JsonProcessingException e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            throw new AwsTokenDecodeException("Failed to decode JWT token");
        }
    }
}

