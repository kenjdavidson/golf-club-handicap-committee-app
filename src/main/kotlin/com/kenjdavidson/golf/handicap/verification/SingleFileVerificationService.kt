package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate

interface SingleFileVerificationStep {
    fun process(context: VerificationContext): VerificationContext
}

@Service
class DateMatchVerificationService {
    fun verify(pdfDates: Collection<LocalDate>, golfCanadaDates: Set<LocalDate>): DateVerificationResult {
        val sortedPdfDates = pdfDates.toSet().sortedDescending()
        val mismatchedDates = sortedPdfDates.filterNot(golfCanadaDates::contains)
        val matchedCount = sortedPdfDates.size - mismatchedDates.size
        val comparedCount = sortedPdfDates.size
        val matchPercentage = if (comparedCount == 0) {
            0
        } else {
            ((matchedCount.toDouble() / comparedCount.toDouble()) * 100).toInt()
        }
        val status = when {
            matchPercentage >= 90 -> VerificationStatus.PASS
            matchPercentage >= 80 -> VerificationStatus.WARNING
            else -> VerificationStatus.ALERT
        }

        return DateVerificationResult(
            status = status,
            matchPercentage = matchPercentage,
            matchedCount = matchedCount,
            comparedCount = comparedCount,
            mismatchedDates = mismatchedDates
        )
    }
}

@Component
@Order(100)
class ParsePdfVerificationStep(
    private val pdfRoundParser: PdfRoundParser
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext =
        context.copy(parsedHistory = pdfRoundParser.parse(context.fileBytes))
}

@Component
@Order(200)
class LookupHistoryVerificationStep(
    private val memberLookupService: GolfCanadaMemberLookupService,
    private val historyLookupService: GolfCanadaHistoryLookupService
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext {
        val parsedHistory = context.parsedHistory ?: return context
        val matchedMember = memberLookupService.findMember(
            parsedHistory = parsedHistory,
            authenticatedUser = context.authenticatedUser,
            accessToken = context.authenticatedUser.accessToken
        )
        val golfCanadaDates = historyLookupService.getHistoryDates(
            matchedMember?.individualId,
            context.authenticatedUser.accessToken
        )
        return context.copy(matchedMember = matchedMember, golfCanadaDates = golfCanadaDates)
    }
}

@Component
@Order(300)
class CompareDatesVerificationStep(
    private val dateMatchVerificationService: DateMatchVerificationService
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext {
        val parsedHistory = context.parsedHistory ?: return context
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

@Service
class SingleFileVerificationService(
    steps: List<SingleFileVerificationStep>
) {
    private val orderedSteps = steps

    fun verify(
        fileName: String,
        fileBytes: ByteArray,
        authenticatedUser: GolfCanadaAuthenticatedUser
    ): FileVerificationResult {
        val context = orderedSteps.fold(
            VerificationContext(
                fileName = fileName,
                fileBytes = fileBytes,
                authenticatedUser = authenticatedUser
            )
        ) { current, step -> step.process(current) }

        return context.result
            ?: throw IllegalStateException("Verification pipeline completed without producing a result.")
    }
}
