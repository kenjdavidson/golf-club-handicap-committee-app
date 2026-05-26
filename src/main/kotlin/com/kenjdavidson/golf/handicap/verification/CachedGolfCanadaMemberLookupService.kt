package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
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
            parsedHistory.memberId.orEmpty()
        ).joinToString("|")

        return cache.computeIfAbsent(cacheKey) { resolveMatch(parsedHistory) }
    }

    private fun resolveMatch(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch? {
        val nameMatch = parsedHistory.playerName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { searchByName(it) }

        if (nameMatch != null) {
            return nameMatch
        }

        return parsedHistory.memberId
            ?.trim()
            ?.toLongOrNull()
            ?.let { profileByMemberId(it, parsedHistory.playerName) }
    }

    private fun searchByName(playerName: String): GolfCanadaMemberMatch? {
        val results = try {
            membersApi.searchMembers(0, 20, playerName)?.members.orEmpty()
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to search Golf Canada members by name.", exception)
        }

        if (results.size > 1) {
            throw NonUniqueMemberFoundException(results)
        }

        if (results.isEmpty()) {
            return null
        }

        val entry = results.first()
        val individualId = entry.individualId ?: return null
        val profile = fetchProfile(individualId)

        return GolfCanadaMemberMatch(
            individualId = individualId,
            fullName = entry.name?.trim()?.takeIf { it.isNotBlank() } ?: UNKNOWN_PLAYER_NAME,
            golfCanadaCardId = profile.cardId,
            profile = profile
        )
    }

    private fun profileByMemberId(individualId: Long, playerName: String?): GolfCanadaMemberMatch? {
        val profile = fetchProfile(individualId)

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

    private companion object {
        private const val UNKNOWN_PLAYER_NAME = "Unknown Player"
    }
}
