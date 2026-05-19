package com.kenjdavidson.golf.handicap.verification

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

        val matchedMember = memberLookupService.findMember(
            parsedHistory = parsedHistory,
            authenticatedUser = context.authenticatedUser
        )
        val golfCanadaDates = historyLookupService.getHistoryDates(
            matchedMember?.individualId
        )

        return context.copy(matchedMember = matchedMember, golfCanadaDates = golfCanadaDates)
    }
}
