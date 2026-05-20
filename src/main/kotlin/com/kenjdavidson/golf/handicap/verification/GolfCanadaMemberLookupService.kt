package com.kenjdavidson.golf.handicap.verification

interface GolfCanadaMemberLookupService {
    fun findMember(parsedHistory: ParsedPlayerHistory): GolfCanadaMemberMatch?
}
