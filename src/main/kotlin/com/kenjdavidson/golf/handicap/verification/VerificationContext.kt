package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import java.time.LocalDate

data class VerificationContext(
    val fileName: String,
    val fileBytes: ByteArray,
    val authenticatedUser: GolfCanadaAuthenticatedUser,
    val parsedHistory: ParsedPlayerHistory? = null,
    val matchedMember: GolfCanadaMemberMatch? = null,
    val golfCanadaDates: Set<LocalDate> = emptySet(),
    val result: FileVerificationResult? = null
)
