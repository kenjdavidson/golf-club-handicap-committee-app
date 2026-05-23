package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.i18n.AppMessages

interface SingleFileVerificationStep {
    fun process(context: VerificationContext): VerificationContext
    fun statusMessageKey(): String = "step.status.processing"
    fun statusMessage(): String = AppMessages.translateCurrent(statusMessageKey())
}
