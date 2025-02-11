package com.assignment.subscription.controller;

import com.assignment.subscription.controller.error.ApplicationExceptionHandler;
import com.assignment.subscription.model.dto.SubscriptionDto;
import com.assignment.subscription.service.SubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {
    private static final String SUBSCRIPTION_URL = "/subscribe";

    private MockMvc mockMvc;

    @InjectMocks
    private SubscriptionController controller;

    @Mock
    private SubscriptionService subscriptionService;


    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ApplicationExceptionHandler(), controller).build();
    }

    @Test
    void subscribeWithSuccessTest() throws Exception {
        var subscriptionDto = generateSubscriptionDto();
        Mockito.when(subscriptionService.createSubscription(Mockito.any(SubscriptionDto.class))).thenReturn(subscriptionDto);
        this.mockMvc.perform(post(SUBSCRIPTION_URL)
                .content(buildBody(subscriptionDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void subscribeWithNoBodyTest() throws Exception {
        this.mockMvc.perform(post(SUBSCRIPTION_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void subscribeWithInvalidDateTest() throws Exception {
        var subscriptionDto = generateSubscriptionDto();
        subscriptionDto.setStartDate(null);
        this.mockMvc.perform(post(SUBSCRIPTION_URL)
                        .content(buildBody(subscriptionDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("{\"startDate\":\"Start date cannot be null\"}"));
    }

    @Test
    void subscribeWithNoUserIdTest() throws Exception {
        var subscriptionDto = generateSubscriptionDto();
        subscriptionDto.setUserId(null);
        this.mockMvc.perform(post(SUBSCRIPTION_URL)
                        .content(buildBody(subscriptionDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("{\"userId\":\"User id cannot be null\"}"));
    }




    private String buildBody(SubscriptionDto subscriptionDto) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(subscriptionDto);
    }

    private SubscriptionDto generateSubscriptionDto() {
        var subscriptionDto = new SubscriptionDto();
        subscriptionDto.setUserId(1L);
        subscriptionDto.setStartDate(LocalDate.of(2025, 01, 01));
        subscriptionDto.setEndDate(LocalDate.of(2026, 01, 01));
        return subscriptionDto;
    }


}