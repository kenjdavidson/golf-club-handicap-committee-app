package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CachedGolfCanadaMemberLookupService(
    private val membersApiFactory: GolfCanadaMembersApiFactory
) : GolfCanadaMemberLookupService {
    private val cache = ConcurrentHashMap<String, GolfCanadaMemberMatch?>()

    override fun findMember(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val cacheKey = listOf(
            parsedHistory.playerName.orEmpty(),
            parsedHistory.memberId.orEmpty(),
            parsedHistory.homeCourse.orEmpty()
        ).joinToString("|")

        return cache.computeIfAbsent(cacheKey) {
            val individualId = parsedHistory.memberId?.trim()?.toLongOrNull() ?: return@computeIfAbsent null
            val parsedMemberId = individualId.toString()
            val profileHomeCourse = try {
                membersApiFactory.create().getProfile(individualId).homeCourse
            } catch (exception: Exception) {
                throw VerificationProcessingException("Unable to retrieve Golf Canada member profile.", exception)
            }

            if (!matchesHomeCourse(parsedHistory.homeCourse, profileHomeCourse)) {
                return@computeIfAbsent null
            }

            GolfCanadaMemberMatch(
                individualId = individualId,
                fullName = parsedHistory.playerName?.takeIf { it.isNotBlank() },
                golfCanadaCardId = parsedMemberId,
                homeCourse = profileHomeCourse
            )
        }
    }

    private fun matchesHomeCourse(parsedHomeCourse: String?, profileHomeCourse: String?): Boolean {
        if (parsedHomeCourse.isNullOrBlank() || profileHomeCourse.isNullOrBlank()) {
            return true
        }
        val left = parsedHomeCourse.trim().lowercase()
        val right = profileHomeCourse.trim().lowercase()
        return left.contains(right) || right.contains(left)
    }
}
