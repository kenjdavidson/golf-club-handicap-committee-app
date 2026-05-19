package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import java.time.LocalDate

const val MAX_VERIFICATION_ROUNDS = 20

data class ParsedRound(
    val playedDate: LocalDate,
    val playDistance: String?,
    val courseGroup: String?,
    val primaryClub: String?
)

data class ParsedPlayerHistory(
    val playerName: String?,
    val memberId: String?,
    val homeCourse: String?,
    val rounds: List<ParsedRound>
)

data class GolfCanadaMemberMatch(
    val individualId: Long,
    val fullName: String?,
    val golfCanadaCardId: String?,
    val homeCourse: String?
)

enum class VerificationStatus {
    PASS,
    WARNING,
    ALERT
}

data class DateVerificationResult(
    val status: VerificationStatus,
    val matchPercentage: Int,
    val matchedCount: Int,
    val comparedCount: Int,
    val mismatchedDates: List<LocalDate>
)

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

data class VerificationContext(
    val fileName: String,
    val fileBytes: ByteArray,
    val authenticatedUser: GolfCanadaAuthenticatedUser,
    val parsedHistory: ParsedPlayerHistory? = null,
    val matchedMember: GolfCanadaMemberMatch? = null,
    val golfCanadaDates: Set<LocalDate> = emptySet(),
    val result: FileVerificationResult? = null
)
