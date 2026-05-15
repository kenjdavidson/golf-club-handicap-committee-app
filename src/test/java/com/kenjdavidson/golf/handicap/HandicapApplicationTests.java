package com.kenjdavidson.golf.handicap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that verifies the Spring application context loads successfully.
 *
 * <p>This test starts the full application context (including the embedded
 * server) on a random port to confirm that all beans wire up correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HandicapApplicationTests {

    /**
     * Verifies that the Spring application context starts without errors.
     * If any bean definition or configuration is broken this test will fail.
     */
    @Test
    void contextLoads() {
        // The mere fact that the application context started (and this method
        // was invoked) is the assertion.
    }
}
