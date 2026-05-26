package com.kenjdavidson.golf.handicap.settings

import com.kenjdavidson.golf.handicap.verification.ParserType
import org.springframework.stereotype.Component

@Component
class AppSettings {
    @Volatile
    var selectedParserType: ParserType = ParserType.PDF_TYPE_1
}
