package com.assignment.subscription.cache;

import java.time.Duration;
import java.time.Instant;


public record CachedToken(String token, Instant expiryTime) {
    public boolean isExpiringSoon() {
        return Instant.now().plus(Duration.ofDays(1)).isAfter(expiryTime);  // Refresh 1 day before expiry
    }
}
