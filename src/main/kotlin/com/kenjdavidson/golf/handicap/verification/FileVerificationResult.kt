package com.kenjdavidson.golf.handicap.verification

import java.time.LocalDate

data class FileVerificationResult(
    val playerName: String?,
    val memberId: String?,
    val matchedMember: GolfCanadaMemberMatch?,
    val status: VerificationStatus,
    val matchPercentage: Int,
    val matchedCount: Int,
    val comparedCount: Int,
    val mismatchedDates: List<LocalDate>,
    val notes: List<String>,
    val parsedRounds: List<ParsedRound> = emptyList(),
    val roundComparisons: List<RoundComparison> = emptyList()
)
