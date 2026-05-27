package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
import com.kenjdavidson.golf.handicap.settings.AppSettings
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CachedGolfCanadaMemberLookupService(
    private val membersApi: MembersApi,
    private val appSettings: AppSettings
) : GolfCanadaMemberLookupService {
    private val cache = ConcurrentHashMap<String, GolfCanadaMemberMatch?>()

    override fun findMember(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val effectiveHomeCourse = resolveHomeCourse(parsedHistory.homeCourse)
        val cacheKey = listOf(
            parsedHistory.playerName.orEmpty(),
            parsedHistory.memberId.orEmpty(),
            effectiveHomeCourse.orEmpty()
        ).joinToString("|")

        return cache.computeIfAbsent(cacheKey) { resolveMatch(parsedHistory, effectiveHomeCourse) }
    }

    private fun resolveMatch(parsedHistory: ParsedPlayerHistory, homeCourse: String?): GolfCanadaMemberMatch? {
        val nameMatch = parsedHistory.playerName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { searchByName(it, homeCourse) }

        if (nameMatch != null) {
            return nameMatch
        }

        return parsedHistory.memberId
            ?.trim()
            ?.toLongOrNull()
            ?.let { profileByMemberId(it, parsedHistory.playerName, homeCourse) }
    }

    private fun searchByName(playerName: String, homeCourse: String?): GolfCanadaMemberMatch? {
        val results = try {
            membersApi.searchMembers(0, 20, playerName)?.members.orEmpty()
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to search Golf Canada members by name.", exception)
        }

        val matched = results.filter { matchesHomeCourse(homeCourse, it.club) }

        if (matched.size > 1) {
            throw NonUniqueMemberFoundException(matched)
        }

        if (matched.isEmpty()) {
            return null
        }

        val entry = matched.first()
        val individualId = entry.individualId ?: return null
        val profile = fetchProfile(individualId)

        return GolfCanadaMemberMatch(
            individualId = individualId,
            fullName = entry.name?.trim()?.takeIf { it.isNotBlank() } ?: UNKNOWN_PLAYER_NAME,
            golfCanadaCardId = profile.cardId,
            profile = profile
        )
    }

    private fun profileByMemberId(individualId: Long, playerName: String?, homeCourse: String?): GolfCanadaMemberMatch? {
        val profile = fetchProfile(individualId)
        if (!matchesHomeCourse(homeCourse, profile.homeCourse)) {
            return null
        }

        return GolfCanadaMemberMatch(
            individualId = individualId,
            fullName = playerName?.trim()?.takeIf { it.isNotBlank() } ?: UNKNOWN_PLAYER_NAME,
            golfCanadaCardId = profile.cardId,
            profile = profile
        )
    }

    private fun fetchProfile(individualId: Long): Profile {
        return try {
            membersApi.getProfile(individualId)
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to retrieve Golf Canada member profile.", exception)
        }
    }

    private fun resolveHomeCourse(parsedHomeCourse: String?): String? {
        return parsedHomeCourse
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: appSettings.defaultHomeCourse
    }

    private fun matchesHomeCourse(parsedHomeCourse: String?, profileHomeCourse: String?): Boolean {
        if (parsedHomeCourse.isNullOrBlank() || profileHomeCourse.isNullOrBlank()) {
            return true
        }
        val left = parsedHomeCourse.trim().lowercase()
        val right = profileHomeCourse.trim().lowercase()
        return left.contains(right) || right.contains(left)
    }

    private companion object {
        private const val UNKNOWN_PLAYER_NAME = "Unknown Player"
    }
}
