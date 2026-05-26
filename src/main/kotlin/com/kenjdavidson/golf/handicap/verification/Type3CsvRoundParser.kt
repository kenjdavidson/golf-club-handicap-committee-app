package com.kenjdavidson.golf.handicap.verification

import org.springframework.stereotype.Service

@Service
class Type3CsvRoundParser : RoundParser {

    override fun type(): ParserType = ParserType.CSV_TYPE_3

    override fun parse(fileBytes: ByteArray): ParsedPlayerHistory {
        throw VerificationProcessingException("CSV Type 3 parser is not yet implemented.")
    }
}
