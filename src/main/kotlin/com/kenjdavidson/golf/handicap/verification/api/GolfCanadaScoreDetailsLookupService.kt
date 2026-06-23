package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails

interface GolfCanadaScoreDetailsLookupService {
    fun getScoreDetails(scoreId: Long): ScoreDetails?
}
