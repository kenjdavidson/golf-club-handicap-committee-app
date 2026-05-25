package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.MemberSearchEntry
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CachedGolfCanadaMemberLookupService(
    private val membersApi: MembersApi
) : GolfCanadaMemberLookupService {
    private val cache = ConcurrentHashMap<String, GolfCanadaMemberMatch?>()

    override fun findMember(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val cacheKey = listOf(
            parsedHistory.playerName.orEmpty(),
            parsedHistory.memberId.orEmpty(),
            parsedHistory.homeCourse.orEmpty()
        ).joinToString("|")

        return cache.computeIfAbsent(cacheKey) { resolveMatch(parsedHistory) }
    }

    private fun resolveMatch(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val nameMatch = parsedHistory.playerName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { searchByName(it, parsedHistory.homeCourse) }

        if (nameMatch != null) {
            return nameMatch
        }

        return parsedHistory.memberId
            ?.trim()
            ?.toLongOrNull()
            ?.let { profileByMemberId(it, parsedHistory) }
    }

    private fun searchByName(playerName: String, homeCourse: String?): GolfCanadaMemberMatch? {
        val results = try {
            membersApi.searchMembers(0, 20, playerName)?.members.orEmpty()
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to search Golf Canada members by name.", exception)
        }

        val matched = results.filter { matchesHomeCourse(homeCourse, it.club) }

        if (matched.size != 1) {
            return null
        }

        val entry = matched.first()
        val individualId = entry.individualId ?: return null
        val profileHomeCourse = fetchProfileHomeCourse(individualId)

        return GolfCanadaMemberMatch(
            individualId = individualId,
            fullName = entry.name?.trim()?.takeIf { it.isNotBlank() } ?: UNKNOWN_PLAYER_NAME,
            golfCanadaCardId = individualId.toString(),
            homeCourse = profileHomeCourse
        )
    }

    private fun profileByMemberId(individualId: Long, parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val profileHomeCourse = fetchProfileHomeCourse(individualId)

        if (!matchesHomeCourse(parsedHistory.homeCourse, profileHomeCourse)) {
            return null
        }

        return GolfCanadaMemberMatch(
            individualId = individualId,
            fullName = parsedHistory.playerName?.trim()?.takeIf { it.isNotBlank() } ?: UNKNOWN_PLAYER_NAME,
            golfCanadaCardId = individualId.toString(),
            homeCourse = profileHomeCourse
        )
    }

    private fun fetchProfileHomeCourse(individualId: Long): String? {
        return try {
            membersApi.getProfile(individualId).homeCourse
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to retrieve Golf Canada member profile.", exception)
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

    private companion object {
        private const val UNKNOWN_PLAYER_NAME = "Unknown Player"
    }
}
