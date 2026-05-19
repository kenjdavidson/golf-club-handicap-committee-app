package com.kenjdavidson.golf.handicap.verification

import java.time.LocalDate

interface GolfCanadaHistoryLookupService {
    fun getHistoryDates(individualId: Long?): Set<LocalDate>
}
