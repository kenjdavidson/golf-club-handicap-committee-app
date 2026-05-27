package com.kenjdavidson.golf.handicap.verification.file

import com.kenjdavidson.golf.handicap.verification.ParsedPlayerHistory

interface RoundParser {
    fun parse(fileBytes: ByteArray): ParsedPlayerHistory
}
