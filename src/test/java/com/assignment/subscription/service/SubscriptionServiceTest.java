package com.assignment.subscription.service;

import com.assignment.subscription.exception.TokenRefreshException;
import com.assignment.subscription.exception.ChronologicalDateException;
import com.assignment.subscription.exception.InsuranceCreationException;
import com.assignment.subscription.exception.UserNotFoundException;
import com.assignment.subscription.mapper.SubscriptionMapper;
import com.assignment.subscription.model.dto.SubscriptionDto;
import com.assignment.subscription.model.entity.Subscription;
import com.assignment.subscription.model.entity.User;
import com.assignment.subscription.repository.SubscriptionRepository;
import com.assignment.subscription.repository.UserRepository;
import com.assignment.subscription.service.validation.SubscriptionValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNzcwMDAwMDIyLCJpYXQiOjE1MTYyMzkwMjJ9.LlhG15cQCy-hCE6xVWKMFCUhkxtl1QTVp8IpSINXs7w";

    @InjectMocks
    private SubscriptionService test;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private SubscriptionValidationService subscriptionValidation;

    @Spy
    private final SubscriptionMapper subscriptionMapper = Mappers.getMapper(SubscriptionMapper.class);


    @Test
    void createSubscriptionSuccessfullyTest() throws UserNotFoundException, ChronologicalDateException, TokenRefreshException, InsuranceCreationException {
        var validSubscriptionDto = generateSubscriptionDto();
        var user = generateUser();
        // Arrange
        doNothing().when(subscriptionValidation).validateDates(validSubscriptionDto);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(tokenService.getApiToken()).thenReturn(TOKEN);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription subscription = invocation.getArgument(0);
            subscription.setId(1L);
            return subscription;
        });

        // Act
        SubscriptionDto result = test.createSubscription(validSubscriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals(validSubscriptionDto.getUserId(), result.getUserId());
        assertEquals(validSubscriptionDto.getStartDate(), result.getStartDate());
        assertEquals(validSubscriptionDto.getEndDate(), result.getEndDate());
        assertEquals(validSubscriptionDto.isAutorenewEnabled(), result.isAutorenewEnabled());

        verify(subscriptionValidation).validateDates(validSubscriptionDto);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createSubscription_invalidDatesTest() throws ChronologicalDateException {
        var validSubscriptionDto = generateSubscriptionDto();
        var user = generateUser();
        // Arrange
        doThrow(new ChronologicalDateException("Start date must be before end date!")).when(subscriptionValidation).validateDates(any(SubscriptionDto.class));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        assertThrowsExactly(ChronologicalDateException.class, () -> test.createSubscription(validSubscriptionDto));
    }

    @Test
    void createSubscription_invalidUserIdTest() {
        var validSubscriptionDto = generateSubscriptionDto();
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrowsExactly(UserNotFoundException.class, () -> test.createSubscription(validSubscriptionDto));
    }

    @Test
    void createSubscription_insuranceIssueFailsTest() throws  ChronologicalDateException, TokenRefreshException {
        var validSubscriptionDto = generateSubscriptionDto();
        var user = generateUser();
        // Arrange
        doNothing().when(subscriptionValidation).validateDates(validSubscriptionDto);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(tokenService.getApiToken()).thenThrow(new TokenRefreshException("Failed to refresh API token") );

        // Act
        assertThrowsExactly(InsuranceCreationException.class, () -> test.createSubscription(validSubscriptionDto));
    }

    private SubscriptionDto generateSubscriptionDto() {
        var subscriptionDto = new SubscriptionDto();
        subscriptionDto.setUserId(1L);
        subscriptionDto.setStartDate(LocalDate.of(2025, 01, 01));
        subscriptionDto.setEndDate(LocalDate.of(2026, 01, 01));
        return subscriptionDto;
    }

    private User generateUser() {
        var user = new User();
        user.setId(1L);
        return user;
    }


}