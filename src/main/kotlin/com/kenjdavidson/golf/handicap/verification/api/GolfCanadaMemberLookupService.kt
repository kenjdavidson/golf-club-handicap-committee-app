package com.kenjdavidson.golf.handicap.verification.api

import com.kenjdavidson.golf.handicap.verification.GolfCanadaMemberMatch
import com.kenjdavidson.golf.handicap.verification.ParsedPlayerHistory

interface GolfCanadaMemberLookupService {
    fun findMember(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch?
}
