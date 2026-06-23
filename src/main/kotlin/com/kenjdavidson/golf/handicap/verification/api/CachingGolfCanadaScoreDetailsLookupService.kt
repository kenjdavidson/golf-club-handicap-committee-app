package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.golfcanada.api.ScoresApi
import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.concurrent.ConcurrentHashMap

@Service
class CachingGolfCanadaScoreDetailsLookupService(
    private val scoresApi: ScoresApi
) : GolfCanadaScoreDetailsLookupService {
    private val cache = ConcurrentHashMap<Long, ScoreDetails>()

    override fun getScoreDetails(scoreId: Long): ScoreDetails? {
        return try {
            cache.computeIfAbsent(scoreId) { scoresApi.getScoreDetails(it) }
        } catch (exception: HttpClientErrorException.NotFound) {
            null
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to retrieve Golf Canada score details.", exception)
        }
    }
}
