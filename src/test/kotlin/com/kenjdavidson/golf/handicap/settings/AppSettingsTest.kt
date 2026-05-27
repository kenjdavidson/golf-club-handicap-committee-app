package com.kenjdavidson.golf.handicap.settings

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import com.kenjdavidson.golf.handicap.golfcanada.model.User
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.verification.ParsedPlayerHistory
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.file.Path

class AppSettingsTest {
    @TempDir
    lateinit var tempHome: Path

    private val parserOne = ParserOne()
    private val parserTwo = ParserTwo()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `loads persisted user settings for the authenticated user`() {
        authenticate("committee.user")
        withTemporaryUserHome(tempHome) {
            val repository = UserSettingsRepository()
            repository.save(
                "committee.user",
                UserSettings(
                    selectedParserClassName = parserTwo.javaClass.name,
                    maxRounds = 7,
                    defaultHomeCourse = "Snake Point"
                )
            )
            val appSettings = AppSettings(listOf(parserOne, parserTwo), VerificationProperties(20), repository)

            appSettings.loadCurrentUserSettings()

            assertEquals(parserTwo.javaClass.name, appSettings.selectedParser.javaClass.name)
            assertEquals(7, appSettings.maxRounds)
            assertEquals("Snake Point", appSettings.defaultHomeCourse)
        }
    }

    @Test
    fun `persists setting edits for the authenticated user`() {
        authenticate("committee.user")
        withTemporaryUserHome(tempHome) {
            val repository = UserSettingsRepository()
            val appSettings = AppSettings(listOf(parserOne, parserTwo), VerificationProperties(20), repository)
            appSettings.loadCurrentUserSettings()

            appSettings.selectedParser = parserTwo
            appSettings.maxRounds = 9
            appSettings.defaultHomeCourse = "  Blue Springs  "

            val persisted = repository.load("committee.user")
            assertEquals(parserTwo.javaClass.name, persisted?.selectedParserClassName)
            assertEquals(9, persisted?.maxRounds)
            assertEquals("Blue Springs", persisted?.defaultHomeCourse)
        }
    }

    private fun authenticate(username: String) {
        val authenticatedUser = GolfCanadaAuthenticatedUser(
            authToken = AuthToken().accessToken("access-token").user(
                User()
                    .username(username)
                    .fullName("Committee User")
                    .email("committee.user@example.com")
            ),
            usernameValue = username,
            displayNameValue = "Committee User",
            emailValue = "committee.user@example.com",
            authoritiesValue = emptyList()
        )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            authenticatedUser,
            null,
            authenticatedUser.authorities
        )
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

    private class ParserOne : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = error("Not used in AppSettings tests.")
    }

    private class ParserTwo : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = error("Not used in AppSettings tests.")
    }
}
