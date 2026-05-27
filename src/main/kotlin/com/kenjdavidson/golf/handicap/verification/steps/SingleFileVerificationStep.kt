package com.kenjdavidson.golf.handicap.verification.steps

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.VerificationContext

interface SingleFileVerificationStep {
    fun process(context: VerificationContext): VerificationContext
    fun statusMessageKey(): String = "step.status.processing"
    fun statusMessage(): String = AppMessages.translateCurrent(statusMessageKey())
}
