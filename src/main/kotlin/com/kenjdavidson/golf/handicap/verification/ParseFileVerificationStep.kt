package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.settings.AppSettings
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(100)
class ParseFileVerificationStep(
    private val roundParsers: List<RoundParser>,
    private val appSettings: AppSettings
) : SingleFileVerificationStep {

    override fun process(context: VerificationContext): VerificationContext {
        val parser = roundParsers.find { it.type() == appSettings.selectedParserType }
            ?: throw VerificationProcessingException(
                "No parser registered for type: ${appSettings.selectedParserType}"
            )
        return context.copy(parsedHistory = parser.parse(context.fileBytes))
    }

    override fun statusMessageKey(): String = "step.status.parsePdf"
}
