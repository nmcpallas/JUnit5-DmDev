package com.unit.dto;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateSubscriptionDtoTest {

    @ParameterizedTest
    @MethodSource("dataProvider")
    void checkBuilder(int userId, String name, String provider, Instant expirationDate) {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(provider)
                .expirationDate(expirationDate)
                .build();

        assertAll(
                () -> assertEquals(createSubscriptionDto.getUserId(), userId),
                () -> assertEquals(createSubscriptionDto.getName(), name),
                () -> assertEquals(createSubscriptionDto.getProvider(), provider),
                () -> assertEquals(createSubscriptionDto.getExpirationDate(), expirationDate)
        );
    }

    private static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(1, "name", Provider.GOOGLE.name(), Instant.now())
        );
    }
}
