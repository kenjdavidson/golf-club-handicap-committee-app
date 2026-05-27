package com.kenjdavidson.golf.handicap.verification

import java.time.LocalDate

data class ParsedRound(
    val playedDate: LocalDate,
    val playDistance: String?,
    val courseGroup: String?,
    val primaryClub: String?
)
