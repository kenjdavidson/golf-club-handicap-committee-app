package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Component

@Component
class VerificationSettings(verificationProperties: VerificationProperties) {
    @Volatile
    var maxRounds: Int = verificationProperties.maxRounds
        set(value) {
            field = value.coerceAtLeast(1)
        }
}
