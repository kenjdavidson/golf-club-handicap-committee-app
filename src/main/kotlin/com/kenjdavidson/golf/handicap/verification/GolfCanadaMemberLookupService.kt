package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser

interface GolfCanadaMemberLookupService {
    fun findMember(
        parsedHistory: ParsedPlayerHistory,
        authenticatedUser: GolfCanadaAuthenticatedUser
    ): GolfCanadaMemberMatch?
}
