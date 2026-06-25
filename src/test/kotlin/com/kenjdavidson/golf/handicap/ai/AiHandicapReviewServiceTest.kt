package com.kenjdavidson.golf.handicap.ai

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.golfcanada.model.HoleScore
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.MemberProfile
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaHistoryLookupService
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaScoreDetailsLookupService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

class AiHandicapReviewServiceTest {

    private val historyLookupService = mock(GolfCanadaHistoryLookupService::class.java)
    private val scoreDetailsLookupService = mock(GolfCanadaScoreDetailsLookupService::class.java)
    private val verificationProperties = VerificationProperties(maxRounds = 3)
    private val aiService = mock(AiIntegrationService::class.java)

    private val service = AiHandicapReviewService(
        historyLookupService,
        scoreDetailsLookupService,
        verificationProperties
    )

    /** Kotlin-safe any() for AiReviewRequest: Mockito returns null but Kotlin needs non-null. */
    private fun anyRequest(): AiReviewRequest =
        ArgumentMatchers.any(AiReviewRequest::class.java) ?: AiReviewRequest(resultWithProfile())

    /** Builds a result with a matched profile carrying the given [individualId]. */
    private fun resultWithProfile(individualId: Long? = 42L): FileVerificationResult =
        FileVerificationResult(
            memberProfile = MemberProfile(
                fullName = "Jim Test",
                cardId = "12345",
                isMatched = individualId != null,
                profile = individualId?.let { Profile().individualId(it) }
            ),
            status = VerificationStatus.PASS,
            matchPercentage = 100,
            matchedCount = 2,
            comparedCount = 2,
            mismatchedDates = emptyList(),
            notes = emptyList(),
            parsedRounds = emptyList(),
            roundComparisons = emptyList()
        )

    @Test
    fun `reviewAsync builds request containing member profile and calls aiService`() {
        `when`(historyLookupService.getHistory(42L)).thenReturn(emptyList())
        `when`(aiService.generate(anyRequest())).thenReturn("No irregularities found.")

        val result = service.reviewAsync(resultWithProfile(), aiService).get()

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `reviewAsync includes score details in request when available`() {
        val historyEntry = HistoryEntry().id(101L).date(LocalDateTime.of(2026, 5, 1, 9, 0))
        `when`(historyLookupService.getHistory(42L)).thenReturn(listOf(historyEntry))

        val scoreDetails = ScoreDetails()
            .id(101L)
            .course("Blue Springs")
            .playDate(LocalDateTime.of(2026, 5, 1, 9, 0))
            .gross(85)
            .par(72)
            .differential(11.5)
            .holeScores(
                listOf(
                    HoleScore().gross(5).par(4),
                    HoleScore().gross(4).par(4),
                    HoleScore().gross(6).par(5)
                )
            )
        `when`(scoreDetailsLookupService.getScoreDetails(101L)).thenReturn(scoreDetails)

        var capturedRequest: AiReviewRequest? = null
        `when`(aiService.generate(anyRequest())).thenAnswer { inv ->
            capturedRequest = inv.getArgument(0)
            "No irregularities found."
        }

        service.reviewAsync(resultWithProfile(), aiService).get()

        val captured = capturedRequest
        assertTrue(captured != null, "aiService.generate should have been called")
        assertTrue(
            captured!!.scoreDetails.any { it.course == "Blue Springs" },
            "Request should include Blue Springs score details"
        )
        assertTrue(
            captured.scoreDetails.any { it.gross == 85 },
            "Request should include gross score"
        )
    }

    @Test
    fun `reviewAsync returns ai response`() {
        `when`(historyLookupService.getHistory(42L)).thenReturn(emptyList())
        `when`(aiService.generate(anyRequest()))
            .thenReturn("Some irregularities may need further review.")

        val result = service.reviewAsync(resultWithProfile(), aiService).get()

        assertTrue(result.contains("irregularities"))
    }

    @Test
    fun `reviewAsync skips history fetch when profile has no individualId`() {
        `when`(aiService.generate(anyRequest())).thenReturn("No irregularities found.")

        val result = service.reviewAsync(resultWithProfile(individualId = null), aiService).get()

        assertFalse(result.isBlank())
        // historyLookupService should NOT have been called
        org.mockito.Mockito.verify(historyLookupService, org.mockito.Mockito.never()).getHistory(
            org.mockito.ArgumentMatchers.anyLong()
        )
    }

    @Test
    fun `reviewAsync limits score lookups to maxRounds`() {
        val entries = (1L..10L).map { id -> HistoryEntry().id(id) }
        `when`(historyLookupService.getHistory(42L)).thenReturn(entries)
        entries.forEach { e ->
            `when`(scoreDetailsLookupService.getScoreDetails(e.id!!)).thenReturn(
                ScoreDetails().id(e.id).gross(80).par(72).differential(8.0)
            )
        }
        `when`(aiService.generate(anyRequest())).thenReturn("ok")

        service.reviewAsync(resultWithProfile(), aiService).get()

        // Only the first maxRounds=3 entries should be fetched
        org.mockito.Mockito.verify(scoreDetailsLookupService, org.mockito.Mockito.times(3))
            .getScoreDetails(org.mockito.ArgumentMatchers.anyLong())
    }
}
