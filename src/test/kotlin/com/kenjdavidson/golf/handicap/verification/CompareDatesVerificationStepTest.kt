package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.LocalDateTime

class CompareDatesVerificationStepTest {
    private val step = CompareDatesVerificationStep(DateMatchVerificationService())

    @Test
    fun `builds date grouped comparisons including scheduled only and golf canada only rows`() {
        val context = VerificationContext(
            fileName = "rounds.pdf",
            fileBytes = byteArrayOf(1),
            authenticatedUser = mock(GolfCanadaAuthenticatedUser::class.java),
            parsedHistory = ParsedPlayerHistory(
                playerName = "Adderley, Jim",
                memberId = "6104412250",
                homeCourse = "Blue Springs",
                rounds = listOf(
                    parsedRound(LocalDate.of(2026, 5, 10), "Scheduled A"),
                    parsedRound(LocalDate.of(2026, 5, 9), "Scheduled B")
                )
            ),
            golfCanadaHistory = listOf(
                HistoryEntry().date(LocalDateTime.of(2026, 5, 10, 9, 0)).course("GC A"),
                HistoryEntry().date(LocalDateTime.of(2026, 5, 8, 9, 0)).course("GC B")
            )
        )

        val result = step.process(context).result ?: error("Expected verification result")

        assertEquals(listOf(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 9), LocalDate.of(2026, 5, 8)), result.roundComparisons.map { it.date })
        assertEquals(true, result.roundComparisons[0].isMatched)
        assertEquals("Scheduled B", result.roundComparisons[1].scheduledRound?.playDistance)
        assertNull(result.roundComparisons[1].golfCanadaEntry)
        assertNull(result.roundComparisons[2].scheduledRound)
        assertEquals("GC B", result.roundComparisons[2].golfCanadaEntry?.course)
    }

    private fun parsedRound(date: LocalDate, distance: String) = ParsedRound(
        playedDate = date,
        playDistance = distance,
        courseGroup = null,
        primaryClub = null,
        playingPartners = emptyList()
    )
}
