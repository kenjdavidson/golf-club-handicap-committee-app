package com.kenjdavidson.golf.handicap.verification

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VerificationProperties(
    @Value("\${app.verification.max-rounds:20}")
    val maxRounds: Int
)
