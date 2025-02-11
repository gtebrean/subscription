package com.assignment.subscription.mapper;

import com.assignment.subscription.model.dto.SubscriptionDto;
import com.assignment.subscription.model.entity.Subscription;
import org.mapstruct.Mapping;

@org.mapstruct.Mapper(
        componentModel = "spring"
)
public interface SubscriptionMapper {
    @Mapping(source = "user.id", target = "userId")
    SubscriptionDto toDto(Subscription subscription);
    Subscription toEntity(SubscriptionDto subscriptionDto);
}
