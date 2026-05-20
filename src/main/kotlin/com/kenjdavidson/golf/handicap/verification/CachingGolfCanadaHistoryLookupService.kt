package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class CachingGolfCanadaHistoryLookupService(
    private val membersApi: MembersApi,
    private val verificationProperties: VerificationProperties,
    private val clock: Clock = Clock.systemDefaultZone()
) : GolfCanadaHistoryLookupService {
    private val cache = ConcurrentHashMap<Pair<Long, LocalDate>, Set<LocalDate>>()

    override fun getHistoryDates(individualId: Long?): Set<LocalDate> {
        if (individualId == null) {
            return emptySet()
        }
        val requestDate = LocalDate.now(clock)
        val cacheKey = individualId to requestDate

        return cache.computeIfAbsent(cacheKey) {
            try {
                membersApi.getHistory(individualId, 0, verificationProperties.maxRounds)
                    ?.data
                    .orEmpty()
                    .mapNotNull { it.date?.toLocalDate() }
                    .toSet()
            } catch (exception: Exception) {
                throw VerificationProcessingException("Unable to retrieve Golf Canada history.", exception)
            }
        }
    }
}
