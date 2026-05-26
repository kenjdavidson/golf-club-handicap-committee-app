package com.kenjdavidson.golf.handicap.verification

interface RoundParser {
    fun parse(fileBytes: ByteArray): ParsedPlayerHistory
}
