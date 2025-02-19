package com.assignment.subscription.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {
    @Bean
    public Cache<String, CachedToken> cache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS) // Tokens expire after 1 hour
                .maximumSize(1) // Limit cache size
                .build();
    }
}
