package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

interface GolfCanadaMemberLookupService {
    fun findMember(
        parsedHistory: ParsedPlayerHistory,
        authenticatedUser: GolfCanadaAuthenticatedUser,
        accessToken: String
    ): GolfCanadaMemberMatch?
}

interface GolfCanadaHistoryLookupService {
    fun getHistoryDates(individualId: Long?, accessToken: String): Set<LocalDate>
}

@Component
class GolfCanadaMembersApiFactory(
    private val baseApiClient: ApiClient
) {
    fun create(accessToken: String): MembersApi {
        val apiClient = ApiClient().setBasePath(baseApiClient.basePath)
        apiClient.setBearerToken(accessToken)
        return MembersApi(apiClient)
    }
}

@Service
class MockGolfCanadaMemberLookupService(
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
            val user = authenticatedUser.golfCanadaUser
            val individualId = user.id ?: user.authUserId ?: return@computeIfAbsent null
            val profileHomeCourse = runCatching {
                membersApiFactory.create(accessToken).getProfile(individualId).homeCourse
            }.getOrNull()

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
        if (parsedMemberId.isNullOrBlank() || authenticatedCardId.isNullOrBlank()) {
            return true
        }
        return parsedMemberId.trim() == authenticatedCardId.trim()
    }

    private fun matchesName(parsedName: String?, authenticatedName: String?): Boolean {
        if (parsedName.isNullOrBlank() || authenticatedName.isNullOrBlank()) {
            return true
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
            "${parts.getOrNull(1).orEmpty()} ${parts.getOrNull(0).orEmpty()}"
        } else {
            trimmed
        }
        return normalized.lowercase().replace(Regex("\\s+"), " ").trim()
    }
}

@Service
class CachingGolfCanadaHistoryLookupService(
    private val membersApiFactory: GolfCanadaMembersApiFactory
) : GolfCanadaHistoryLookupService {
    private val cache = ConcurrentHashMap<Long, Set<LocalDate>>()

    override fun getHistoryDates(individualId: Long?, accessToken: String): Set<LocalDate> {
        if (individualId == null) {
            return emptySet()
        }
        return cache.computeIfAbsent(individualId) {
            membersApiFactory.create(accessToken)
                .getHistory(individualId, 0, 20)
                ?.data
                .orEmpty()
                .mapNotNull { it.date?.toLocalDate() }
                .toSet()
        }
    }
}
