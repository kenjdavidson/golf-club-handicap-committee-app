package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.settings.AppSettings
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class CachingGolfCanadaHistoryLookupService(
    private val membersApi: MembersApi,
    private val appSettings: AppSettings,
    private val clock: Clock = Clock.systemDefaultZone()
) : GolfCanadaHistoryLookupService {
    private val cache = ConcurrentHashMap<Pair<Long, LocalDate>, List<HistoryEntry>>()

    override fun getHistory(individualId: Long?): List<HistoryEntry> {
        if (individualId == null) {
            return emptyList()
        }
        val requestDate = LocalDate.now(clock)
        val cacheKey = individualId to requestDate

        return cache.computeIfAbsent(cacheKey) {
            try {
                membersApi.getHistory(individualId, 0, appSettings.maxRounds * GOLF_CANADA_HISTORY_LOOKBACK_MULTIPLIER)
                    ?.data
                    .orEmpty()
            } catch (exception: Exception) {
                throw VerificationProcessingException("Unable to retrieve Golf Canada history.", exception)
            }
        }
    }

    companion object {
        private const val GOLF_CANADA_HISTORY_LOOKBACK_MULTIPLIER = 3
    }
}
