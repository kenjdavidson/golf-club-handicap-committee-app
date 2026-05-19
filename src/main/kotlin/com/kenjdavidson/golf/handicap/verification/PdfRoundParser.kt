package com.kenjdavidson.golf.handicap.verification

interface PdfRoundParser {
    fun parse(pdfBytes: ByteArray): ParsedPlayerHistory
}
