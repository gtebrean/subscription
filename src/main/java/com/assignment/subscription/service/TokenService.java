package com.assignment.subscription.service;

import com.assignment.subscription.cache.CachedToken;
import com.assignment.subscription.exception.TokenDecodeException;
import com.assignment.subscription.exception.TokenRefreshException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private static final String MOCKED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNzcwMDAwMDIyLCJpYXQiOjE1MTYyMzkwMjJ9.LlhG15cQCy-hCE6xVWKMFCUhkxtl1QTVp8IpSINXs7w";
    private static final String SECRET_NAME = "api_token"; // Token Name
    private static final long TOKEN_REFRESH_INTERVAL = 6 * 24 * 60 * 60L; // 6 days in seconds

    @Value("${insurance.token.url}")
    private String tokenUrl;

    private final Cache<String, CachedToken> cache;


    public String getApiToken() throws TokenRefreshException {
        var cachedToken = cache.getIfPresent(SECRET_NAME);
        if (cachedToken == null || cachedToken.isExpiringSoon()) {
            log.info("Token missing or expiring soon. Refreshing token...");
            refreshApiToken();
            cachedToken = cache.getIfPresent(SECRET_NAME);
        }
        return (cachedToken != null) ? cachedToken.token() : null;
    }
    
    public synchronized void refreshApiToken() throws TokenRefreshException {
        try {
            log.info("Attempting to refresh API token...");

            HttpHeaders headers = new HttpHeaders();
            // in case it was using the same token to pas security fot the GET request
            // headers.set("Authorization", "Bearer " + cachedToken.token());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            //HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, headers);
            // Execute GET request
            // RestTemplate restTemplate = new RestTemplate();
            // ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.GET, requestEntity, String.class);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put(SECRET_NAME, MOCKED_TOKEN);

            log.info("Response: {}", responseBody);


            // Update cache only if token is valid
            Instant expiryTime = getTokenExpiry(responseBody.get(SECRET_NAME));
            cache.put(SECRET_NAME, new CachedToken(responseBody.get(SECRET_NAME), expiryTime));

            log.info("API token refreshed successfully.");
        } catch (Exception e) {
            log.error("Failed to refresh API token: {}", e.getMessage());
            throw new TokenRefreshException("Failed to refresh API token");
        }
    }

    /**
     * Decodes the JWT and extracts the expiration time.
     */
    private Instant getTokenExpiry(String jwtToken) throws TokenDecodeException {
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
            throw new TokenDecodeException("Failed to decode JWT token");
        }
    }
}

