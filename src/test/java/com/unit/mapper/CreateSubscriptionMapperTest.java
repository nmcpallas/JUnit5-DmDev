package com.unit.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.mapper.CreateSubscriptionMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateSubscriptionMapperTest {

    @ParameterizedTest
    @MethodSource("dataProvider")
    void checkMapping(CreateSubscriptionDto object) {
        CreateSubscriptionMapper createSubscriptionMapper = CreateSubscriptionMapper.getInstance();
        Subscription objectAfterMap  = createSubscriptionMapper.map(object);

        assertAll(
                () -> assertEquals(object.getUserId(), objectAfterMap.getUserId()),
                () -> assertEquals(object.getName(), objectAfterMap.getName()),
                () -> assertEquals(Provider.findByNameOpt(object.getProvider()).orElse(null), objectAfterMap.getProvider()),
                () -> assertEquals(object.getExpirationDate(), objectAfterMap.getExpirationDate()),
                () -> assertEquals(Status.ACTIVE, objectAfterMap.getStatus())
        );
    }

    private static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("name")
                        .provider("provider")
                        .expirationDate(Instant.now())
                        .build()),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("name")
                        .provider(Provider.GOOGLE.name())
                        .expirationDate(Instant.now())
                        .build())
        );
    }
}
