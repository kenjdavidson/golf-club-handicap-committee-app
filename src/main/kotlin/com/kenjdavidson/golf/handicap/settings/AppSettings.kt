package com.kenjdavidson.golf.handicap.settings

import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import org.springframework.stereotype.Component

@Component
class AppSettings(parsers: List<RoundParser>) {
    @Volatile
    var selectedParser: RoundParser = parsers.first()

    @Volatile
    var defaultHomeCourse: String? = null
        set(value) {
            field = value?.trim()?.takeIf { it.isNotBlank() }
        }
}
