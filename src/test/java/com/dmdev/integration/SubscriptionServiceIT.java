package com.dmdev.integration;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.service.SubscriptionService;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubscriptionServiceIT extends IntegrationTestBase{
    SubscriptionDao subscriptionDao;
    SubscriptionService subscriptionService;
    @BeforeEach
    void cleanData() throws SQLException {
        super.cleanData();
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                SubscriptionDao.getInstance(),
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                Clock.systemUTC()
        );
    }

    @Test
    void subscriptionIsNotExistsUpsertMethod() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "Ivan", Provider.GOOGLE.name(), Instant.MAX);

        Subscription actualSubscription = subscriptionService.upsert(subscriptionDto);

        Subscription expectedSubscription = CreateSubscriptionMapper.getInstance().map(subscriptionDto);
        expectedSubscription.setId(actualSubscription.getId());

        assertEquals(expectedSubscription, actualSubscription);
    }

    @Test
    void usuallyCancel() {
        Subscription subscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.MAX, Status.ACTIVE);
        subscriptionDao.insert(subscription);

        subscriptionService.cancel(subscription.getId());

        List<Subscription> actualSubscription = subscriptionDao.findByUserId(subscription.getUserId());

        assertEquals(Status.CANCELED, actualSubscription.get(0).getStatus());
    }

    @Test
    void usuallyExpire() {
        Subscription subscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.MAX, Status.ACTIVE);
        subscriptionDao.insert(subscription);

        subscriptionService.expire(subscription.getId());

        List<Subscription> actualSubscription = subscriptionDao.findByUserId(subscription.getUserId());

        assertEquals(Status.EXPIRED, actualSubscription.get(0).getStatus());
        assertEquals(
                Instant.now().truncatedTo(ChronoUnit.DAYS),
                actualSubscription.get(0).getExpirationDate().truncatedTo(ChronoUnit.DAYS));
    }

    private CreateSubscriptionDto getSubscriptionDto(Integer userId, String Ivan, String GOOGLE, Instant max) {
        return CreateSubscriptionDto.builder()
                .userId(userId)
                .name(Ivan)
                .provider(GOOGLE)
                .expirationDate(max)
                .build();
    }

    private Subscription getSubscription(Integer id, Integer userId, String name, Provider provider, Instant expirationDate, Status status) {
        return Subscription.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .provider(provider)
                .expirationDate(expirationDate)
                .status(status)
                .build();
    }
}
