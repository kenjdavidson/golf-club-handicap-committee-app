package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.model.MemberSearchEntry

class NonUniqueMemberFoundException(
    val candidates: List<MemberSearchEntry>
) : RuntimeException("Multiple Golf Canada members matched the search criteria. Please select the correct member.")
