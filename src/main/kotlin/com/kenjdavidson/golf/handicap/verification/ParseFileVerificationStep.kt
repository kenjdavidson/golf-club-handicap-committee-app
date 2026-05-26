package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.settings.AppSettings
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(100)
class ParseFileVerificationStep(
    private val appSettings: AppSettings
) : SingleFileVerificationStep {

    override fun process(context: VerificationContext): VerificationContext {
        return context.copy(parsedHistory = appSettings.selectedParser.parse(context.fileBytes))
    }

    override fun statusMessageKey(): String = "step.status.parsePdf"
}
