package com.kenjdavidson.golf.handicap.verification.steps

import com.kenjdavidson.golf.handicap.verification.VerificationContext
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaHistoryLookupService
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaMemberLookupService
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(200)
class LookupHistoryVerificationStep(
    private val memberLookupService: GolfCanadaMemberLookupService,
    private val historyLookupService: GolfCanadaHistoryLookupService
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext {
        val parsedHistory = context.parsedHistory
            ?: throw VerificationProcessingException("Verification pipeline missing parsed history before history lookup.")

        val matchedMember = memberLookupService.findMember(parsedHistory)
        val golfCanadaHistory = historyLookupService.getHistory(
            matchedMember?.individualId
        )

        return context.copy(matchedMember = matchedMember, golfCanadaHistory = golfCanadaHistory)
    }

    override fun statusMessageKey(): String = "step.status.lookupHistory"
}
