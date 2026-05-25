package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.Profile

data class GolfCanadaMemberMatch(
    val individualId: Long,
    val fullName: String?,
    val golfCanadaCardId: String?,
    val profile: Profile? = null
) {
    val homeCourse: String? get() = profile?.homeCourse
}
