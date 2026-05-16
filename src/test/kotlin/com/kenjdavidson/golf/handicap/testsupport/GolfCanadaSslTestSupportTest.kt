package com.kenjdavidson.golf.handicap.testsupport

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.client.ResourceAccessException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.time.Duration
import java.util.function.Supplier

class GolfCanadaSslTestSupportTest {

    @Test
    fun executeWithConnectionResetRetry_retriesAndReturnsValue() {
        var attempts = 0

        val result = GolfCanadaSslTestSupport.executeWithConnectionResetRetry(
            Supplier {
                attempts++
                if (attempts < 3) {
                    throw ResourceAccessException("I/O error", SocketException("Connection reset"))
                }
                "token"
            },
            3,
            Duration.ZERO
        )

        assertEquals("token", result)
        assertEquals(3, attempts)
    }

    @Test
    fun executeWithConnectionResetRetry_doesNotRetryNonResetErrors() {
        var attempts = 0

        assertThrows(ResourceAccessException::class.java) {
            GolfCanadaSslTestSupport.executeWithConnectionResetRetry(
                Supplier {
                    attempts++
                    throw ResourceAccessException("I/O error", ConnectException("Connection refused"))
                },
                3,
                Duration.ZERO
            )
        }

        assertEquals(1, attempts)
    }

    @Test
    fun isConnectionReset_detectsNestedCauseMessage() {
        val exception = ResourceAccessException("I/O error", IOException("Transport failed", SocketException("Connection reset by peer")))
        assertTrue(GolfCanadaSslTestSupport.isConnectionReset(exception))
    }

    @Test
    fun isConnectionReset_returnsFalseForNonResetErrors() {
        assertFalse(GolfCanadaSslTestSupport.isConnectionReset(ResourceAccessException("I/O error", ConnectException("Connection refused"))))
    }
}
