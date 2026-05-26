package com.kenjdavidson.golf.handicap.verification

interface RoundParser {
    fun type(): ParserType
    fun parse(fileBytes: ByteArray): ParsedPlayerHistory
}
