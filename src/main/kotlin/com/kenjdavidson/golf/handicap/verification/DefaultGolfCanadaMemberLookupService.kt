package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class DefaultGolfCanadaMemberLookupService(
    private val membersApiFactory: GolfCanadaMembersApiFactory
) : GolfCanadaMemberLookupService {
    private val cache = ConcurrentHashMap<String, GolfCanadaMemberMatch?>()

    override fun findMember(
        parsedHistory: ParsedPlayerHistory,
        authenticatedUser: GolfCanadaAuthenticatedUser,
        accessToken: String
    ): GolfCanadaMemberMatch? {
        val cacheKey = listOf(
            parsedHistory.playerName.orEmpty(),
            parsedHistory.memberId.orEmpty(),
            parsedHistory.homeCourse.orEmpty(),
            authenticatedUser.username
        ).joinToString("|")

        return cache.computeIfAbsent(cacheKey) {
            if (parsedHistory.memberId.isNullOrBlank() && parsedHistory.playerName.isNullOrBlank()) {
                return@computeIfAbsent null
            }
            val user = authenticatedUser.golfCanadaUser
            val individualId = user.id ?: user.authUserId ?: return@computeIfAbsent null
            val profileHomeCourse = try {
                membersApiFactory.create(accessToken).getProfile(individualId).homeCourse
            } catch (exception: Exception) {
                throw VerificationProcessingException("Unable to retrieve Golf Canada member profile.", exception)
            }

            if (!matchesMemberId(parsedHistory.memberId, authenticatedUser.golfCanadaCardId)) {
                return@computeIfAbsent null
            }
            if (!matchesName(parsedHistory.playerName, authenticatedUser.displayName)) {
                return@computeIfAbsent null
            }
            if (!matchesHomeCourse(parsedHistory.homeCourse, profileHomeCourse)) {
                return@computeIfAbsent null
            }

            GolfCanadaMemberMatch(
                individualId = individualId,
                fullName = authenticatedUser.displayName,
                golfCanadaCardId = authenticatedUser.golfCanadaCardId,
                homeCourse = profileHomeCourse
            )
        }
    }

    private fun matchesMemberId(parsedMemberId: String?, authenticatedCardId: String?): Boolean {
        if (parsedMemberId.isNullOrBlank()) {
            return true
        }
        if (authenticatedCardId.isNullOrBlank()) {
            return false
        }
        return parsedMemberId.trim() == authenticatedCardId.trim()
    }

    private fun matchesName(parsedName: String?, authenticatedName: String?): Boolean {
        if (parsedName.isNullOrBlank()) {
            return true
        }
        if (authenticatedName.isNullOrBlank()) {
            return false
        }
        return normalizeName(parsedName) == normalizeName(authenticatedName)
    }

    private fun matchesHomeCourse(parsedHomeCourse: String?, profileHomeCourse: String?): Boolean {
        if (parsedHomeCourse.isNullOrBlank() || profileHomeCourse.isNullOrBlank()) {
            return true
        }
        val left = parsedHomeCourse.trim().lowercase()
        val right = profileHomeCourse.trim().lowercase()
        return left.contains(right) || right.contains(left)
    }

    private fun normalizeName(value: String): String {
        val trimmed = value.trim()
        val normalized = if (trimmed.contains(",")) {
            val parts = trimmed.split(",", limit = 2)
            listOf(parts.getOrNull(1).orEmpty(), parts.getOrNull(0).orEmpty())
                .filter { it.isNotBlank() }
                .joinToString(" ")
        } else {
            trimmed
        }
        return normalized.lowercase().replace(Regex("\\s+"), " ").trim()
    }
}
