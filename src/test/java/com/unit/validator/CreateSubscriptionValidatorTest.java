package com.unit.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CreateSubscriptionValidatorTest {
    private final CreateSubscriptionValidator createSubscriptionValidator = CreateSubscriptionValidator.getInstance();


    @Test
    void userIdInvalid() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(null, "name", Provider.GOOGLE.name(), Instant.MAX);

        List<Error> errors = createSubscriptionValidator.validate(subscriptionDto).getErrors();

        assertEquals(1, errors.size());
        assertEquals(100, errors.get(0).getCode());
        assertEquals("userId is invalid", errors.get(0).getMessage());
    }

    @Test
    void nameInvalid() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "", Provider.GOOGLE.name(), Instant.MAX);

        List<Error> errors = createSubscriptionValidator.validate(subscriptionDto).getErrors();

        assertEquals(1, errors.size());
        assertEquals(101, errors.get(0).getCode());
        assertEquals("name is invalid", errors.get(0).getMessage());
    }

    @Test
    void providerInvalid() {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "name", "something", Instant.MAX);

        List<Error> errors = createSubscriptionValidator.validate(subscriptionDto).getErrors();

        assertEquals(1, errors.size());
        assertEquals(102, errors.get(0).getCode());
        assertEquals("provider is invalid", errors.get(0).getMessage());
    }

    @ParameterizedTest
    @MethodSource("expirationDateDataProvider")
    void expirationDateInvalid(Instant expirationDate) {
        CreateSubscriptionDto subscriptionDto = getSubscriptionDto(1, "name", Provider.GOOGLE.name(), expirationDate);

        List<Error> errors = createSubscriptionValidator.validate(subscriptionDto).getErrors();

        assertEquals(1, errors.size());
        assertEquals(103, errors.get(0).getCode());
        assertEquals("expirationDate is invalid", errors.get(0).getMessage());
    }

    @ParameterizedTest
    @MethodSource("expirationDateDataProvider")
    void allParametersInvalid(Instant expirationDate) {
        CreateSubscriptionDto build = getSubscriptionDto(null, "", "something", expirationDate);

        List<Error> errors = createSubscriptionValidator.validate(build).getErrors();

        assertAll(
                () -> assertEquals(4, errors.size()),
                () -> assertEquals(100, errors.get(0).getCode()),
                () -> assertEquals("userId is invalid", errors.get(0).getMessage()),
                () -> assertEquals(101, errors.get(1).getCode()),
                () -> assertEquals("name is invalid", errors.get(1).getMessage()),
                () -> assertEquals(102, errors.get(2).getCode()),
                () -> assertEquals("provider is invalid", errors.get(2).getMessage()),
                () -> assertEquals(103, errors.get(3).getCode()),
                () -> assertEquals("expirationDate is invalid", errors.get(3).getMessage())
        );
    }

    @Test
    void allParametersValid() {
        CreateSubscriptionDto build = getSubscriptionDto(1, "name", Provider.GOOGLE.name(), Instant.MAX);

        assertFalse(createSubscriptionValidator.validate(build).hasErrors());
    }


    private static Stream<Arguments> expirationDateDataProvider() {
        return Stream.of(
                Arguments.arguments((Object) null),
                Arguments.arguments(Instant.now())
        );
    }

    private CreateSubscriptionDto getSubscriptionDto(Integer userId, String name, String GOOGLE, Instant max) {
        return CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(GOOGLE)
                .expirationDate(max)
                .build();
    }
}
