package com.unit.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.service.SubscriptionService;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void subscriptionIsExistsUpsertMethod() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "Ivan", Provider.GOOGLE.name(), Instant.MAX);

        Subscription subscription = getSubscription(1, 1, "Ivan",
                Provider.valueOf(Provider.GOOGLE.name()),
                Instant.MAX,
                Status.EXPIRED);

        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription);
        ValidationResult validationResult = new ValidationResult();

        doReturn(validationResult).when(createSubscriptionValidator).validate(subscriptionDto);
        doReturn(subscriptionList).when(subscriptionDao).findByUserId(subscriptionDto.getUserId());
        doReturn(subscription).when(subscriptionDao).upsert(subscription);

        Subscription actualSubscription = subscriptionService.upsert(subscriptionDto);

        Assertions.assertNotNull(actualSubscription);
        Assertions.assertEquals(subscriptionDto.getUserId(), actualSubscription.getUserId());
        Assertions.assertEquals(subscriptionDto.getName(), actualSubscription.getName());
        Assertions.assertEquals(subscriptionDto.getProvider(), actualSubscription.getProvider().name());
        Assertions.assertEquals(subscriptionDto.getExpirationDate(), actualSubscription.getExpirationDate());
        Assertions.assertEquals(Status.ACTIVE, actualSubscription.getStatus());
    }

    @Test
    void subscriptionIsNotExistsUpsertMethod() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "Ivan", Provider.GOOGLE.name(), Instant.MAX);

        Subscription subscriptionMap = CreateSubscriptionMapper.getInstance().map(subscriptionDto);

        List<Subscription> subscriptionList = new ArrayList<>();

        ValidationResult validationResult = new ValidationResult();

        doReturn(validationResult).when(createSubscriptionValidator).validate(subscriptionDto);
        doReturn(subscriptionList).when(subscriptionDao).findByUserId(subscriptionDto.getUserId());
        doReturn(subscriptionMap).when(createSubscriptionMapper).map(subscriptionDto);
        doReturn(subscriptionMap).when(subscriptionDao).upsert(subscriptionMap);

        Subscription actualSubscription = subscriptionService.upsert(subscriptionDto);
        Assertions.assertNotNull(actualSubscription);
        Assertions.assertEquals(subscriptionDto.getUserId(), actualSubscription.getUserId());
        Assertions.assertEquals(subscriptionDto.getName(), actualSubscription.getName());
        Assertions.assertEquals(subscriptionDto.getProvider(), actualSubscription.getProvider().name());
        Assertions.assertEquals(subscriptionDto.getExpirationDate(), actualSubscription.getExpirationDate());
        Assertions.assertEquals(Status.ACTIVE, actualSubscription.getStatus());
    }

    @Test
    void validationExceptionInUpsert() {
        CreateSubscriptionDto build = getSubscriptionDto(null, null, null, null);
        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "userId is invalid"));

        doReturn(validationResult).when(createSubscriptionValidator).validate(any());

        Assertions.assertThrows(ValidationException.class, () -> subscriptionService.upsert(build));
    }

    @Test
    void usuallyCancel() {
        Subscription subscription = getSubscription(1, null, null, null, null, Status.ACTIVE);

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        doReturn(subscription).when(subscriptionDao).update(subscription);

        subscriptionService.cancel(anyInt());

        verify(subscriptionDao, times(1)).update(subscription);
    }

    @Test
    void illegalArgumentExceptionInCancel() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        Assertions.assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(anyInt()));
    }

    @Test
    void subscriptionExceptionInCancel() {
        Subscription subscription = getSubscription(1, null, null, null, null, Status.EXPIRED);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(1);

        Assertions.assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(1), "Only active subscription 1 can be canceled");
    }

    @Test
    void usuallyExpire() {
        SubscriptionDao subscriptionDaoMock = mock(SubscriptionDao.class);
        subscriptionService = new SubscriptionService(subscriptionDaoMock,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                Clock.systemUTC());

        Subscription subscription = getSubscription(1, null, null, null, null, Status.ACTIVE);

        doReturn(Optional.of(subscription)).when(subscriptionDaoMock).findById(anyInt());
        doReturn(subscription).when(subscriptionDaoMock).update(subscription);

        subscriptionService.expire(anyInt());

        Assertions.assertEquals(Status.EXPIRED, subscription.getStatus());
        Assertions.assertEquals(Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.DAYS), subscription.getExpirationDate().truncatedTo(ChronoUnit.DAYS));
        verify(subscriptionDaoMock, times(1)).update(subscription);
    }

    @Test
    void illegalArgumentExceptionInExpire() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        Assertions.assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(anyInt()));
    }

    @Test
    void subscriptionExceptionInExpire() {
        Subscription subscription = getSubscription(1, null, null, null, null, Status.EXPIRED);

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(1);

        Assertions.assertThrows(SubscriptionException.class, () -> subscriptionService.expire(1), "Only active subscription 1 can be canceled");
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
