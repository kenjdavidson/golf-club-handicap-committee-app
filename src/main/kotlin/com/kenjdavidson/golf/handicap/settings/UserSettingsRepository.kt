package com.kenjdavidson.golf.handicap.settings

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.springframework.stereotype.Repository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Repository
class UserSettingsRepository {
    private val objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()

    private val settingsDirectory: Path
        get() = Paths.get(System.getProperty("user.home")).resolve(".handicapapp")
    private val settingsFile: Path
        get() = settingsDirectory.resolve("settings.json")

    @Synchronized
    fun load(username: String): UserSettings? = readAllSettings()[username]

    @Synchronized
    fun save(username: String, userSettings: UserSettings) {
        Files.createDirectories(settingsDirectory)
        val allSettings = readAllSettings().toMutableMap()
        allSettings[username] = userSettings
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), allSettings)
    }

    private fun readAllSettings(): Map<String, UserSettings> {
        if (!Files.exists(settingsFile)) {
            return emptyMap()
        }
        if (Files.size(settingsFile) == 0L) {
            return emptyMap()
        }
        return objectMapper.readValue(settingsFile.toFile(), SETTINGS_TYPE_REFERENCE)
    }

    private companion object {
        val SETTINGS_TYPE_REFERENCE = object : TypeReference<Map<String, UserSettings>>() {}
    }
}
