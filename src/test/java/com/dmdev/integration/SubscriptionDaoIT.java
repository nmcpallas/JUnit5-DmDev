package com.dmdev.integration;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionDaoIT extends IntegrationTestBase {
    SubscriptionDao subscriptionDao;

    @BeforeEach
    void cleanData() throws SQLException {
        super.cleanData();
        subscriptionDao = SubscriptionDao.getInstance();
    }

    @Test
    void findAll() {
        Subscription firstSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);
        Subscription secondSubscription = getSubscription(2, 2, "Danil", Provider.APPLE, Instant.now(), Status.EXPIRED);
        Subscription thirdSubscription = getSubscription(3, 3, "Alex", Provider.GOOGLE, Instant.now(), Status.CANCELED);

        subscriptionDao.insert(firstSubscription);
        subscriptionDao.insert(secondSubscription);
        subscriptionDao.insert(thirdSubscription);

        List<Subscription> subscriptions = subscriptionDao.findAll();

        assertEquals(3, subscriptions.size());
        List<Integer> userIds = subscriptions.stream()
                .map(Subscription::getUserId)
                .toList();
        assertAll(
                () -> assertTrue(userIds.contains(firstSubscription.getUserId())),
                () -> assertTrue(userIds.contains(secondSubscription.getUserId())),
                () -> assertTrue(userIds.contains(thirdSubscription.getUserId()))
        );
    }

    @Test
    void wasFoundById() {
        Subscription expectedSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(expectedSubscription);

        Optional<Subscription> actualSubscription = subscriptionDao.findById(expectedSubscription.getId());

        assertTrue(actualSubscription.isPresent());
        assertEquals(expectedSubscription, actualSubscription.get());
    }

    @Test
    void wasNotFoundById() {
        Subscription expectedSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(expectedSubscription);

        Optional<Subscription> actualSubscription = subscriptionDao.findById(2);

        assertFalse(actualSubscription.isPresent());
    }

    @Test
    void deleteWas() {
        Subscription subscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(subscription);

        assertTrue(subscriptionDao.delete(subscription.getId()));
    }

    @Test
    void deleteWasNot() {
        Subscription subscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(subscription);

        assertFalse(subscriptionDao.delete(2));
    }

    @Test
    void update() {
        Subscription subscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);
        Subscription expectedSubscription = getSubscription(2, 2, "Danil", Provider.APPLE, Instant.now(), Status.EXPIRED);

        subscriptionDao.insert(subscription);

        Subscription actualSubscription = subscriptionDao.update(expectedSubscription);

        expectedSubscription.setId(2);
        assertEquals(expectedSubscription, actualSubscription);
    }

    @Test
    void insert() {
        Subscription expectedSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        Subscription actualSubscription = subscriptionDao.insert(expectedSubscription);

        assertEquals(expectedSubscription, actualSubscription);
    }

    @Test
    void wasFoundByUserId() {
        Subscription expectedSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(expectedSubscription);

        List<Subscription> subscriptions = subscriptionDao.findByUserId(expectedSubscription.getUserId());

        assertEquals(1, subscriptions.size());
        assertEquals(expectedSubscription, subscriptions.get(0));
    }

    @Test
    void wasNotFoundByUserId() {
        Subscription expectedSubscription = getSubscription(1, 1, "Ivan", Provider.GOOGLE, Instant.now(), Status.ACTIVE);

        subscriptionDao.insert(expectedSubscription);

        List<Subscription> subscriptions = subscriptionDao.findByUserId(2);

        assertEquals(0, subscriptions.size());
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
