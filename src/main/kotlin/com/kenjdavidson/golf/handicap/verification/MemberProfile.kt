package com.kenjdavidson.golf.handicap.verification

data class MemberProfile(
    val fullName: String?,
    val cardId: String?,
    val homeCourse: String?,
    val isMatched: Boolean
) {
    companion object {
        fun from(match: GolfCanadaMemberMatch, fallbackName: String? = null, fallbackCardId: String? = null) =
            MemberProfile(
                fullName = match.fullName ?: fallbackName,
                cardId = match.golfCanadaCardId ?: fallbackCardId,
                homeCourse = match.homeCourse,
                isMatched = true
            )

        fun unmatched(fullName: String?, cardId: String?) =
            MemberProfile(
                fullName = fullName,
                cardId = cardId,
                homeCourse = null,
                isMatched = false
            )
    }
}
