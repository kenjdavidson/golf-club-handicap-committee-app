package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class CachingGolfCanadaHistoryLookupService(
    private val membersApi: MembersApi,
    private val verificationSettings: VerificationSettings,
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
                membersApi.getHistory(individualId, 0, verificationSettings.maxRounds)
                    ?.data
                    .orEmpty()
            } catch (exception: Exception) {
                throw VerificationProcessingException("Unable to retrieve Golf Canada history.", exception)
            }
        }
    }
}
