package com.unit.util;

import com.dmdev.util.PropertiesUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

public class PropertiesUtilTest {
    private static final String URL_KEY = "db.url";
    private static final String USER_KEY = "db.user";
    private static final String PASSWORD_KEY = "db.password";
    private static final String DRIVER_KEY = "db.driver";
    private final String expectedUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private final String expectedUser = "sa";
    private final String expectedPassword = "";
    private final String expectedDriver = "org.h2.Driver";


    @Test
    void getCheck() {
        assertAll(
                () -> assertEquals(expectedUrl, PropertiesUtil.get(URL_KEY)),
                () -> assertEquals(expectedUser, PropertiesUtil.get(USER_KEY)),
                () -> assertEquals(expectedPassword, PropertiesUtil.get(PASSWORD_KEY)),
                () -> assertEquals(expectedDriver, PropertiesUtil.get(DRIVER_KEY))
        );
    }
}
