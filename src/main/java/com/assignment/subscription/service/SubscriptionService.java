package com.assignment.subscription.service;

import com.assignment.subscription.exception.TokenRefreshException;
import com.assignment.subscription.exception.ChronologicalDateException;
import com.assignment.subscription.exception.InsuranceCreationException;
import com.assignment.subscription.exception.UserNotFoundException;
import com.assignment.subscription.mapper.SubscriptionMapper;
import com.assignment.subscription.model.dto.SubscriptionDto;
import com.assignment.subscription.repository.SubscriptionRepository;
import com.assignment.subscription.repository.UserRepository;
import com.assignment.subscription.service.validation.SubscriptionValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    public static final String POLICY_REFERENCE = "policy_reference";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String API_TOKEN = "api_token";
    @Value("${insurance.url}")
    private String insuranceUrl;

    private final SubscriptionRepository subscriptionRepository;
    private final TokenService tokenService;
    private final SubscriptionValidationService subscriptionValidation;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;


    public SubscriptionDto createSubscription(SubscriptionDto subscriptionDto) throws UserNotFoundException,
            ChronologicalDateException, InsuranceCreationException {

        var user = userRepository.findById(subscriptionDto.getUserId()).orElseThrow(()->new UserNotFoundException("User with "+subscriptionDto.getUserId()+" not found"));

        subscriptionValidation.validateDates(subscriptionDto);


        var result = callInsuranceAPI(UUID.randomUUID().toString(), subscriptionDto.getStartDate(), subscriptionDto.getEndDate());

        if (!result.is2xxSuccessful()){
            throw new InsuranceCreationException("Fail to create insurance policy for the subscription");
        }
        var entity = subscriptionMapper.toEntity(subscriptionDto);
        entity.setUser(user);
        subscriptionRepository.save(entity);
        return subscriptionMapper.toDto(entity);
    }

    /**
     * Calls the 3rd-party insurance API with the `api_token` retrieved from AWS Secrets Manager.
     *
     * @return
     */
    private HttpStatusCode callInsuranceAPI(String policyReference, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(POLICY_REFERENCE, policyReference);
        requestBody.put(START_DATE, startDate.toString());
        requestBody.put(END_DATE, endDate.toString());

        String apiToken;
        try {
            apiToken = tokenService.getApiToken();
        } catch (TokenRefreshException e) {
            log.error("Failed to retrieve API token from AWS Secrets Manager: {}", e.getMessage(), e);
            return HttpStatus.SERVICE_UNAVAILABLE; // Return HTTP 503 (Service Unavailable)
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_TOKEN, apiToken); // Use token from AWS Secrets Manager

        // Create HTTP request
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);


        log.info("Mock API Call: Sending request to :{}", insuranceUrl);
        log.info("Body: {}", requestEntity.getBody());

        // Mock response
        var response = new ResponseEntity<>("{\"status\": \"success\"}", HttpStatus.CREATED);

        log.info("Mock API Response: {}", response.getBody());
        return response.getStatusCode();
    }
}
