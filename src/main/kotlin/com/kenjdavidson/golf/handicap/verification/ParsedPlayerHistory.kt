package com.kenjdavidson.golf.handicap.verification

data class ParsedPlayerHistory(
    val playerName: String?,
    val memberId: String?,
    val homeCourse: String?,
    val rounds: List<ParsedRound>
)
