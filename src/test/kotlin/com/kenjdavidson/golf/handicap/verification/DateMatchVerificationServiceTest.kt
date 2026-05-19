package com.kenjdavidson.golf.handicap.verification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateMatchVerificationServiceTest {
    private val service = DateMatchVerificationService()

    @Test
    fun `returns pass when date match is at least 90 percent`() {
        val pdfDates = (1..10).map { LocalDate.of(2024, 1, it) }
        val history = pdfDates.toSet() - LocalDate.of(2024, 1, 10)

        val result = service.verify(pdfDates, history)

        assertEquals(VerificationStatus.PASS, result.status)
        assertEquals(90, result.matchPercentage)
    }

    @Test
    fun `returns warning when date match is between 80 and 89 percent`() {
        val pdfDates = (1..10).map { LocalDate.of(2024, 1, it) }
        val history = pdfDates.toSet() - setOf(LocalDate.of(2024, 1, 9), LocalDate.of(2024, 1, 10))

        val result = service.verify(pdfDates, history)

        assertEquals(VerificationStatus.WARNING, result.status)
        assertEquals(80, result.matchPercentage)
    }

    @Test
    fun `returns alert when date match is below 80 percent and reports mismatches`() {
        val pdfDates = (1..10).map { LocalDate.of(2024, 1, it) }
        val history = pdfDates.take(7).toSet()

        val result = service.verify(pdfDates, history)

        assertEquals(VerificationStatus.ALERT, result.status)
        assertEquals(70, result.matchPercentage)
        assertEquals(
            listOf(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 9), LocalDate.of(2024, 1, 8)),
            result.mismatchedDates
        )
    }
}
