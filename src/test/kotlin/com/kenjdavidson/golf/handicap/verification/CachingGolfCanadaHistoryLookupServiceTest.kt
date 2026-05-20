package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class CachingGolfCanadaHistoryLookupServiceTest {
    private val membersApi = mock(MembersApi::class.java)
    private val clock = MutableClock(Instant.parse("2026-05-19T00:00:00Z"), ZoneOffset.UTC)
    private val service = CachingGolfCanadaHistoryLookupService(
        membersApi = membersApi,
        verificationProperties = VerificationProperties(20),
        clock = clock
    )

    @Test
    fun `uses cache for same day and refreshes when request date changes`() {
        `when`(membersApi.getHistory(123L, 0, 20)).thenReturn(
            HistoryResponse().data(
                listOf(HistoryEntry().date(OffsetDateTime.parse("2026-05-18T00:00:00Z")))
            )
        )

        val first = service.getHistoryDates(123L)
        val second = service.getHistoryDates(123L)

        assertEquals(setOf(LocalDate.parse("2026-05-18")), first)
        assertEquals(first, second)
        verify(membersApi, times(1)).getHistory(123L, 0, 20)

        clock.advanceDays(1)
        service.getHistoryDates(123L)

        verify(membersApi, times(2)).getHistory(123L, 0, 20)
    }

    private class MutableClock(
        private var currentInstant: Instant,
        private var currentZone: ZoneId
    ) : Clock() {
        override fun getZone(): ZoneId = currentZone

        override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

        override fun instant(): Instant = currentInstant

        fun advanceDays(days: Long) {
            currentInstant = currentInstant.plusSeconds(days * 24 * 60 * 60)
        }
    }
}
