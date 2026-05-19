package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class CachingGolfCanadaHistoryLookupService(
    private val membersApiFactory: GolfCanadaMembersApiFactory,
    private val verificationProperties: VerificationProperties
) : GolfCanadaHistoryLookupService {
    private val cache = ConcurrentHashMap<Long, Set<LocalDate>>()

    override fun getHistoryDates(individualId: Long?, accessToken: String): Set<LocalDate> {
        if (individualId == null) {
            return emptySet()
        }

        return cache.computeIfAbsent(individualId) {
            try {
                membersApiFactory.create(accessToken)
                    .getHistory(individualId, 0, verificationProperties.maxRounds)
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
