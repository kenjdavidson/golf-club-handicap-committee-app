package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.Profile

data class GolfCanadaMemberMatch(
    val individualId: Long,
    val fullName: String?,
    val golfCanadaCardId: String?,
    val homeCourse: String?,
    val profile: Profile? = null
)
