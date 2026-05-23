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
import java.time.LocalDateTime
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
        val entry = HistoryEntry().date(LocalDateTime.of(2026, 5, 18, 0, 0))
        `when`(membersApi.getHistory(123L, 0, 20)).thenReturn(
            HistoryResponse().data(listOf(entry))
        )

        val first = service.getHistory(123L)
        val second = service.getHistory(123L)

        assertEquals(listOf(entry), first)
        assertEquals(first, second)
        verify(membersApi, times(1)).getHistory(123L, 0, 20)

        clock.advanceDays(1)
        service.getHistory(123L)

        verify(membersApi, times(2)).getHistory(123L, 0, 20)
    }

    @Test
    fun `returns empty list when individualId is null`() {
        val result = service.getHistory(null)
        assertEquals(emptyList<HistoryEntry>(), result)
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
