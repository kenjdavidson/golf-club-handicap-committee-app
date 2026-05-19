package com.kenjdavidson.golf.handicap.verification

data class GolfCanadaMemberMatch(
    val individualId: Long,
    val fullName: String?,
    val golfCanadaCardId: String?,
    val homeCourse: String?
)
