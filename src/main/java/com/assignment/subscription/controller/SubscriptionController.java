package com.assignment.subscription.controller;

import com.assignment.subscription.exception.ChronologicalDateException;
import com.assignment.subscription.exception.InsuranceCreationException;
import com.assignment.subscription.exception.UserNotFoundException;
import com.assignment.subscription.model.dto.SubscriptionDto;
import com.assignment.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscriptionDto subscriptionDto) throws UserNotFoundException, ChronologicalDateException, InsuranceCreationException {
        log.info("Received subscribe request for user with if {}", subscriptionDto.getUserId());
        var dto = subscriptionService.createSubscription(subscriptionDto);
        log.info("Created subscribtion with id {} for user with if {} ", dto.getId(), dto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dto);
    }

}
