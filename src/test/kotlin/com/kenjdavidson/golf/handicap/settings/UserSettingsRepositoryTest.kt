package com.kenjdavidson.golf.handicap.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class UserSettingsRepositoryTest {
    @TempDir
    lateinit var tempHome: Path

    @Test
    fun `saves and loads settings per username`() {
        withTemporaryUserHome(tempHome) {
            val repository = UserSettingsRepository()
            val firstUserSettings = UserSettings(
                selectedParserClassName = "parser.One",
                maxRounds = 12,
                defaultHomeCourse = "Snake Point"
            )
            val secondUserSettings = UserSettings(
                selectedParserClassName = "parser.Two",
                maxRounds = 8,
                defaultHomeCourse = "Blue Springs"
            )

            repository.save("first.user", firstUserSettings)
            repository.save("second.user", secondUserSettings)

            assertEquals(firstUserSettings, repository.load("first.user"))
            assertEquals(secondUserSettings, repository.load("second.user"))
            assertNull(repository.load("unknown.user"))
        }
    }

    private fun withTemporaryUserHome(userHome: Path, block: () -> Unit) {
        val original = System.getProperty("user.home")
        System.setProperty("user.home", userHome.toString())
        try {
            block()
        } finally {
            System.setProperty("user.home", original)
        }
    }
}
