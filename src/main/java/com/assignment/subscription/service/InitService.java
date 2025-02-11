package com.assignment.subscription.service;

import com.assignment.subscription.exception.TokenRefreshException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {
    private final TokenService tokenService;

    @PostConstruct
    public void init() throws TokenRefreshException {
        tokenService.refreshApiToken();
    }
}
