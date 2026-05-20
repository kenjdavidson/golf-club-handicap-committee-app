package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Service
import java.time.LocalDate

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
