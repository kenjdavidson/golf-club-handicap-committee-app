package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry

interface GolfCanadaHistoryLookupService {
    fun getHistory(individualId: Long?): List<HistoryEntry>
}
