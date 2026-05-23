package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser

data class VerificationContext(
    val fileName: String,
    val fileBytes: ByteArray,
    val authenticatedUser: GolfCanadaAuthenticatedUser,
    val parsedHistory: ParsedPlayerHistory? = null,
    val matchedMember: GolfCanadaMemberMatch? = null,
    val golfCanadaHistory: List<HistoryEntry> = emptyList(),
    val result: FileVerificationResult? = null
)
