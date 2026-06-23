package com.kenjdavidson.golf.handicap.settings

import com.kenjdavidson.golf.handicap.ai.AiIntegrationType
import com.kenjdavidson.golf.handicap.ai.AiSettingsService
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope

@SessionScope
@Component
class UserSettingsService(
    private val parsers: List<RoundParser>,
    private val aiSettingsService: AiSettingsService,
    private val userSettingsRepository: UserSettingsRepository? = null,
    verificationProperties: VerificationProperties
) {
    private val defaultParser: RoundParser = parsers.firstOrNull()
        ?: throw IllegalStateException("UserSettingsService requires at least one RoundParser to be registered in the Spring context.")
    private val parsersByClassName: Map<String, RoundParser> = parsers.associateBy { it.javaClass.name }

    @Volatile
    private var selectedParserValue: RoundParser = defaultParser
    var selectedParser: RoundParser
        get() = selectedParserValue
        set(value) {
            selectedParserValue = value
            persistCurrentUserSettings()
        }

    @Volatile
    private var maxRoundsValue: Int = verificationProperties.maxRounds.coerceAtLeast(1)
    var maxRounds: Int
        get() = maxRoundsValue
        set(value) {
            maxRoundsValue = value.coerceAtLeast(1)
            persistCurrentUserSettings()
        }

    @Volatile
    private var defaultHomeCourseValue: String? = null
    var defaultHomeCourse: String?
        get() = defaultHomeCourseValue
        set(value) {
            defaultHomeCourseValue = value?.trim()?.takeIf { it.isNotBlank() }
            persistCurrentUserSettings()
        }

    @PostConstruct
    fun loadCurrentUserSettings() {
        val username = currentUsernameOrNull() ?: return
        val persisted = userSettingsRepository?.load(username) ?: return

        selectedParserValue = persisted.selectedParserClassName
            ?.let { parsersByClassName[it] }
            ?: defaultParser
        maxRoundsValue = (persisted.maxRounds ?: maxRoundsValue).coerceAtLeast(1)
        defaultHomeCourseValue = persisted.defaultHomeCourse?.trim()?.takeIf { it.isNotBlank() }

        aiSettingsService.applySettings(
            integrationType = persisted.aiIntegrationType ?: AiIntegrationType.NONE,
            selectedModelTag = persisted.aiSelectedModelTag
        )
    }

    @PreDestroy
    fun saveCurrentUserSettings() {
        persistCurrentUserSettings()
    }

    /** Explicitly persists the current in-memory settings to the user-home store. */
    fun persistSettings() {
        persistCurrentUserSettings()
    }

    private fun persistCurrentUserSettings() {
        val username = currentUsernameOrNull() ?: return
        userSettingsRepository?.save(
            username = username,
            userSettings = UserSettings(
                selectedParserClassName = selectedParserValue.javaClass.name,
                maxRounds = maxRoundsValue,
                defaultHomeCourse = defaultHomeCourseValue,
                aiIntegrationType = aiSettingsService.integrationType,
                aiSelectedModelTag = aiSettingsService.selectedModelTag
            )
        )
    }

    private fun currentUsernameOrNull(): String? {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? GolfCanadaAuthenticatedUser
            ?: return null
        return principal.username.takeIf { it.isNotBlank() }
    }
}
