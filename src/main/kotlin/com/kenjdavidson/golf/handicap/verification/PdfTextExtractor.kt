package com.kenjdavidson.golf.handicap.verification

interface PdfTextExtractor {
    fun extract(pdfBytes: ByteArray): String
}
