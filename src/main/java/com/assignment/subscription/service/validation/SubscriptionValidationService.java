package com.assignment.subscription.service.validation;

import com.assignment.subscription.exception.ChronologicalDateException;
import com.assignment.subscription.model.dto.SubscriptionDto;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionValidationService {

    public void validateDates(SubscriptionDto subscriptionDto) throws ChronologicalDateException {
        if(subscriptionDto.getStartDate().isAfter(subscriptionDto.getEndDate())) {
            throw new ChronologicalDateException("Start date must be before end date!");
        }
    }
}
