package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.ParsedPlayerHistory
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.file.ParserDefinition
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsViewTest {

    private val parserOne = StructuredParser()
    private val parserTwo = NoShowParser()

    @Test
    fun `renders grouped settings navigation with club management selected by default`() {
        val view = buildView()
        val navigationTabs = readField<Tabs>(view, "navigationTabs")
        val contentArea = readField<VerticalLayout>(view, "contentArea")

        assertEquals("Club Management", readField<Tab>(view, "clubManagementTab").label)
        assertEquals("AI Features", readField<Tab>(view, "aiFeaturesTab").label)
        assertEquals("About", readField<Tab>(view, "aboutTab").label)
        assertEquals(Tabs.Orientation.VERTICAL, navigationTabs.orientation)
        assertEquals(readField<Tab>(view, "clubManagementTab"), navigationTabs.selectedTab)
        assertTrue(containsComponent(contentArea, Select::class.java))
        assertTrue(containsComponent(contentArea, IntegerField::class.java))
        assertTrue(containsComponent(contentArea, TextField::class.java))
        assertFalse(containsText(contentArea, "GitHub Repository"))
    }

    @Test
    fun `updates bound settings and shows about links when selected`() {
        val settingsService = buildSettingsService()
        val view = SettingsView(settingsService, listOf(parserOne, parserTwo))

        val parserTypeSelect = readField<Select<RoundParser>>(view, "parserTypeSelect")
        val maxRoundsField = readField<IntegerField>(view, "maxRoundsField")
        val defaultHomeCourseField = readField<TextField>(view, "defaultHomeCourseField")
        val navigationTabs = readField<Tabs>(view, "navigationTabs")
        val aboutTab = readField<Tab>(view, "aboutTab")
        val contentArea = readField<VerticalLayout>(view, "contentArea")

        parserTypeSelect.value = parserTwo
        maxRoundsField.value = 12
        defaultHomeCourseField.value = "  Glen Abbey  "
        navigationTabs.selectedTab = aboutTab

        assertEquals(parserTwo, settingsService.selectedParser)
        assertEquals(12, settingsService.maxRounds)
        assertEquals("Glen Abbey", settingsService.defaultHomeCourse)
        assertTrue(containsText(contentArea, "GitHub Repository"))
        assertTrue(containsText(contentArea, "Download Latest Release"))
        assertTrue(containsText(contentArea, "Current Version: development"))
        assertTrue(containsComponent(contentArea, Anchor::class.java))
        assertTrue(containsText(contentArea, "Contact: Open a GitHub issue for support or feature requests."))
    }

    private fun buildView(): SettingsView {
        return SettingsView(buildSettingsService(), listOf(parserOne, parserTwo))
    }

    private fun buildSettingsService(): UserSettingsService {
        return UserSettingsService(
            parsers = listOf(parserOne, parserTwo),
            userSettingsRepository = null,
            verificationProperties = VerificationProperties(20)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> readField(target: Any, fieldName: String): T {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(target) as T
    }

    private fun containsText(component: Component, expected: String): Boolean {
        return when (component) {
            is Tab -> component.label == expected
            is Anchor -> component.text == expected
            is Paragraph -> component.text == expected
            else -> false
        } || component.children.anyMatch { child -> containsText(child, expected) }
    }

    private fun containsComponent(component: Component, expectedType: Class<out Component>): Boolean {
        return expectedType.isInstance(component) || component.children.anyMatch { child ->
            containsComponent(child, expectedType)
        }
    }

    @ParserDefinition(
        displayNameKey = "settings.parser.type.pdfType1.name",
        descriptionKey = "settings.parser.type.pdfType1.description"
    )
    private class StructuredParser : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory {
            return ParsedPlayerHistory(null, null, null, emptyList())
        }
    }

    @ParserDefinition(
        displayNameKey = "settings.parser.type.pdfType2.name",
        descriptionKey = "settings.parser.type.pdfType2.description"
    )
    private class NoShowParser : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory {
            return ParsedPlayerHistory(null, null, null, emptyList())
        }
    }
}
