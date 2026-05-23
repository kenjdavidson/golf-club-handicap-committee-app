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

        val golfCanadaDates = context.golfCanadaHistory
            .mapNotNull { it.date?.toLocalDate() }
            .toSet()

        val dateResult = dateMatchVerificationService.verify(
            pdfDates = parsedHistory.rounds.map { it.playedDate },
            golfCanadaDates = golfCanadaDates
        )

        val entriesByDate = context.golfCanadaHistory
            .mapNotNull { entry -> entry.date?.toLocalDate()?.let { date -> date to entry } }
            .groupBy({ it.first }, { it.second })

        val roundComparisons = parsedHistory.rounds.map { round ->
            val matches = entriesByDate[round.playedDate]
            RoundComparison(
                pdfRound = round,
                golfCanadaEntry = matches?.firstOrNull(),
                isMatched = !matches.isNullOrEmpty()
            )
        }

        val notes = mutableListOf<String>()
        val memberProfile = context.matchedMember?.let {
            MemberProfile.from(it, parsedHistory.playerName, parsedHistory.memberId)
        } ?: MemberProfile.unmatched(parsedHistory.playerName, parsedHistory.memberId)

        if (!memberProfile.isMatched) {
            notes += "Unable to confidently match a Golf Canada member using parsed file details."
        }

        return context.copy(
            result = FileVerificationResult(
                memberProfile = memberProfile,
                status = dateResult.status,
                matchPercentage = dateResult.matchPercentage,
                matchedCount = dateResult.matchedCount,
                comparedCount = dateResult.comparedCount,
                mismatchedDates = dateResult.mismatchedDates,
                notes = notes,
                parsedRounds = parsedHistory.rounds,
                roundComparisons = roundComparisons
            )
        )
    }

    override fun statusMessageKey(): String = "step.status.compareDates"
}
