package com.kenjdavidson.golf.handicap.verification

import java.time.LocalDate

data class DateVerificationResult(
    val status: VerificationStatus,
    val matchPercentage: Int,
    val matchedCount: Int,
    val comparedCount: Int,
    val mismatchedDates: List<LocalDate>
)
