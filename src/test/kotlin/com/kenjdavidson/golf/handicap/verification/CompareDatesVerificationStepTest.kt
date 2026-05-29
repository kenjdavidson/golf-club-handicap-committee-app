package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.verification.steps.CompareDatesVerificationStep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.LocalDateTime

class CompareDatesVerificationStepTest {
    private val step = CompareDatesVerificationStep(DateMatchVerificationService())

    @Test
    fun `builds date grouped comparisons using parsed rounds as the source list`() {
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

        assertEquals(
            listOf(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 9)),
            result.roundComparisons.map { it.date }
        )
        assertEquals(true, result.roundComparisons[0].isMatched)
        assertEquals("Scheduled B", result.roundComparisons[1].scheduledRound?.playDistance)
        assertNull(result.roundComparisons[1].golfCanadaEntry)
    }

    @Test
    fun `matches multiple rounds on the same day by row order`() {
        val sameDay = LocalDate.of(2026, 5, 18)
        val context = VerificationContext(
            fileName = "rounds.pdf",
            fileBytes = byteArrayOf(1),
            authenticatedUser = mock(GolfCanadaAuthenticatedUser::class.java),
            parsedHistory = ParsedPlayerHistory(
                playerName = "Adderley, Jim",
                memberId = "6104412250",
                homeCourse = "Blue Springs",
                rounds = listOf(
                    parsedRound(sameDay, "Scheduled A"),
                    parsedRound(sameDay, "Scheduled B")
                )
            ),
            golfCanadaHistory = listOf(
                HistoryEntry().date(LocalDateTime.of(2026, 5, 18, 9, 0)).course("GC A"),
                HistoryEntry().date(LocalDateTime.of(2026, 5, 18, 14, 0)).course("GC B")
            )
        )

        val result = step.process(context).result ?: error("Expected verification result")

        assertEquals(2, result.roundComparisons.size)
        assertEquals(listOf("Scheduled A", "Scheduled B"), result.roundComparisons.map { it.scheduledRound?.playDistance })
        assertEquals(listOf("GC A", "GC B"), result.roundComparisons.map { it.golfCanadaEntry?.course })
        assertEquals(listOf(true, true), result.roundComparisons.map { it.isMatched })
    }

    @Test
    fun `ignores extra golf canada rounds on a matched date when there are fewer scheduled rounds`() {
        val sameDay = LocalDate.of(2026, 5, 18)
        val context = VerificationContext(
            fileName = "rounds.pdf",
            fileBytes = byteArrayOf(1),
            authenticatedUser = mock(GolfCanadaAuthenticatedUser::class.java),
            parsedHistory = ParsedPlayerHistory(
                playerName = "Adderley, Jim",
                memberId = "6104412250",
                homeCourse = "Blue Springs",
                rounds = listOf(
                    parsedRound(sameDay, "Scheduled A")
                )
            ),
            golfCanadaHistory = listOf(
                HistoryEntry().date(LocalDateTime.of(2026, 5, 18, 9, 0)).course("GC A"),
                HistoryEntry().date(LocalDateTime.of(2026, 5, 18, 14, 0)).course("GC B")
            )
        )

        val result = step.process(context).result ?: error("Expected verification result")

        assertEquals(1, result.roundComparisons.size)
        assertEquals("Scheduled A", result.roundComparisons.single().scheduledRound?.playDistance)
        assertEquals("GC A", result.roundComparisons.single().golfCanadaEntry?.course)
        assertEquals(true, result.roundComparisons.single().isMatched)
    }

    private fun parsedRound(date: LocalDate, distance: String) = ParsedRound(
        playedDate = date,
        playDistance = distance,
        courseGroup = null,
        primaryClub = null
    )
}
