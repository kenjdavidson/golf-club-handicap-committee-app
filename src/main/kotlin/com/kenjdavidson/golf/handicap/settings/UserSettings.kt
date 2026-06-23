package com.kenjdavidson.golf.handicap.settings

import com.kenjdavidson.golf.handicap.ai.AiIntegrationType

data class UserSettings(
    val selectedParserClassName: String? = null,
    val maxRounds: Int? = null,
    val defaultHomeCourse: String? = null,
    val aiIntegrationType: AiIntegrationType? = null,
    val aiSelectedModelTag: String? = null
)
