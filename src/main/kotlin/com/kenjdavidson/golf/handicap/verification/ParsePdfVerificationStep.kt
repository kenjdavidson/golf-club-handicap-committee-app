package com.kenjdavidson.golf.handicap.verification

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(100)
class ParsePdfVerificationStep(
    private val pdfRoundParser: PdfRoundParser
) : SingleFileVerificationStep {
    override fun process(context: VerificationContext): VerificationContext {
        return context.copy(parsedHistory = pdfRoundParser.parse(context.fileBytes))
    }
}
