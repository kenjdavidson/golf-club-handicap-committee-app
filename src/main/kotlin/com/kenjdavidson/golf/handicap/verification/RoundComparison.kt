package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import java.time.LocalDate

data class RoundComparison(
    val date: LocalDate,
    val scheduledRound: ParsedRound?,
    val golfCanadaEntry: HistoryEntry?
) {
    val isMatched: Boolean
        get() = scheduledRound != null && golfCanadaEntry != null
}
