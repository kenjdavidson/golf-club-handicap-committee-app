package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.golfcanada.api.ScoresApi
import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.Collections

@Service
class CachingGolfCanadaScoreDetailsLookupService(
    private val scoresApi: ScoresApi
) : GolfCanadaScoreDetailsLookupService {
    private val cache: MutableMap<Long, ScoreDetails> = Collections.synchronizedMap(
        object : LinkedHashMap<Long, ScoreDetails>(MAX_CACHE_SIZE + 1, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, ScoreDetails>?): Boolean =
                size > MAX_CACHE_SIZE
        }
    )

    override fun getScoreDetails(scoreId: Long): ScoreDetails? {
        cache[scoreId]?.let { return it }
        return try {
            scoresApi.getScoreDetails(scoreId)?.also { cache[scoreId] = it }
        } catch (exception: HttpClientErrorException.NotFound) {
            null
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to retrieve Golf Canada score details.", exception)
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 100
    }
}
