package com.kenjdavidson.golf.handicap.verification

import java.time.LocalDate

data class FileVerificationResult(
    val playerName: String?,
    val memberId: String?,
    val status: VerificationStatus,
    val matchPercentage: Int,
    val matchedCount: Int,
    val comparedCount: Int,
    val mismatchedDates: List<LocalDate>,
    val notes: List<String>
)
