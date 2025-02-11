package com.assignment.subscription.service;

import com.assignment.subscription.cache.CachedToken;
import com.assignment.subscription.exception.TokenRefreshException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNzcwMDAwMDIyLCJpYXQiOjE1MTYyMzkwMjJ9.LlhG15cQCy-hCE6xVWKMFCUhkxtl1QTVp8IpSINXs7w";

    @Spy
    @InjectMocks
    private TokenService test;

    @Spy
    private Cache<String, CachedToken> cache = Caffeine.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS) // Tokens expire after 1 hour
            .maximumSize(1) // Limit cache size
            .build();

    @Test
    void fetchNewTokenAndCacheItTest() throws TokenRefreshException {
        cache.invalidateAll();
        String token = test.getApiToken();

        assertNotNull(token);
        assertEquals(TOKEN, token);

        verify(test, times(1)).refreshApiToken();
    }

    @Test
    void getTokenAnd_failOnRefreshTest() throws TokenRefreshException {
        doThrow(TokenRefreshException.class).when(test).refreshApiToken();

        assertThrows(TokenRefreshException.class, () -> test.getApiToken());

        verify(test, times(1)).refreshApiToken();
    }

    @Test
    void fetchTokenMultipleTimes_ButRefreshCacheOnlyOneTest() throws TokenRefreshException {
        cache.invalidateAll();
        test.getApiToken();

        String token = test.getApiToken();

        assertNotNull(token);
        assertEquals(TOKEN, token);

        verify(test, times(1)).refreshApiToken();
    }

}