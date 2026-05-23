package com.kenjdavidson.golf.handicap.verification

interface SingleFileVerificationStep {
    fun process(context: VerificationContext): VerificationContext
    fun statusMessage(): String = "Processing..."
}
