package com.assignment.subscription.service;

import com.assignment.subscription.exception.AwsTokenRefreshException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {
    private final SecretsManagerService secretsManagerService;

    @PostConstruct
    public void init() throws AwsTokenRefreshException {
        secretsManagerService.refreshApiToken();
    }
}
