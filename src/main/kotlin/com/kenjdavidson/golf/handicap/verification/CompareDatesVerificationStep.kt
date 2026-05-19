package com.kenjdavidson.golf.handicap.verification

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(300)
class CompareDatesVerificationStep(
    private val dateMatchVerificationService: DateMatchVerificationService
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext {
        val parsedHistory = context.parsedHistory
            ?: throw VerificationProcessingException("Verification pipeline missing parsed history before date comparison.")

        val dateResult = dateMatchVerificationService.verify(
            pdfDates = parsedHistory.rounds.map { it.playedDate },
            golfCanadaDates = context.golfCanadaDates
        )

        val notes = mutableListOf<String>()
        if (context.matchedMember == null) {
            notes += "Unable to confidently match a Golf Canada member using parsed file details."
        }

        return context.copy(
            result = FileVerificationResult(
                playerName = parsedHistory.playerName,
                memberId = parsedHistory.memberId,
                status = dateResult.status,
                matchPercentage = dateResult.matchPercentage,
                matchedCount = dateResult.matchedCount,
                comparedCount = dateResult.comparedCount,
                mismatchedDates = dateResult.mismatchedDates,
                notes = notes
            )
        )
    }
}
