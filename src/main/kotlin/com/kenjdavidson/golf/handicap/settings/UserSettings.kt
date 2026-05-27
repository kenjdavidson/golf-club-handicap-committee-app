package com.kenjdavidson.golf.handicap.settings

data class UserSettings(
    val selectedParserClassName: String? = null,
    val maxRounds: Int? = null,
    val defaultHomeCourse: String? = null
)
