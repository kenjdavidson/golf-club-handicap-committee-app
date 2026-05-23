package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry

data class RoundComparison(
    val pdfRound: ParsedRound,
    val golfCanadaEntry: HistoryEntry?,
    val isMatched: Boolean
)
