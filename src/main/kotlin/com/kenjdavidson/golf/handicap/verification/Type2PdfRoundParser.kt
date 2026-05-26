package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Service

@Service
class Type2PdfRoundParser : RoundParser {

    override fun type(): ParserType = ParserType.PDF_TYPE_2

    override fun parse(fileBytes: ByteArray): ParsedPlayerHistory {
        throw VerificationProcessingException("PDF Type 2 parser is not yet implemented.")
    }
}
