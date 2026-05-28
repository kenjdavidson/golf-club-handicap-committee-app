package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.Profile

data class MemberProfile(
    val fullName: String?,
    val cardId: String?,
    val isMatched: Boolean,
    val profile: Profile?
) {
    companion object {
        fun from(match: GolfCanadaMemberMatch, fallbackName: String? = null, fallbackCardId: String? = null) =
            MemberProfile(
                fullName = match.fullName ?: fallbackName,
                cardId = match.golfCanadaCardId ?: fallbackCardId,
                isMatched = true,
                profile = match.profile
            )

        fun unmatched(fullName: String?, cardId: String?) =
            MemberProfile(
                fullName = fullName,
                cardId = cardId,
                isMatched = false,
                profile = null
            )
    }
}
