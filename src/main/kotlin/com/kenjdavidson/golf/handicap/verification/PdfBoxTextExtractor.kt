package com.kenjdavidson.golf.handicap.verification

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component

@Component
class PdfBoxTextExtractor : PdfTextExtractor {
    override fun extract(pdfBytes: ByteArray): String {
        if (pdfBytes.isEmpty()) {
            throw VerificationProcessingException("Uploaded file is empty.")
        }

        return try {
            Loader.loadPDF(pdfBytes).use { document ->
                PDFTextStripper().getText(document)
            }
        } catch (exception: Exception) {
            throw VerificationProcessingException("Unable to extract text from uploaded PDF.", exception)
        }
    }
}
