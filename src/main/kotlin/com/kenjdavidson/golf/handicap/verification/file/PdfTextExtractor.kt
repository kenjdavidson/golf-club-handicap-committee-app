package com.kenjdavidson.golf.handicap.verification.file

interface PdfTextExtractor {
    fun extract(pdfBytes: ByteArray): String
}
