package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.file.ParserDefinition
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "settings", layout = AuthenticatedLayout::class)
@PageTitle("Settings | Handicap Committee App")
@PermitAll
class SettingsView(
    private val appSettings: UserSettingsService,
    private val roundParsers: List<RoundParser>
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()
    private val parserTypeSelect = Select<RoundParser>()
    private val parserTypeDescription = Paragraph()
    private val maxRoundsField = IntegerField()
    private val defaultHomeCourseField = TextField()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        title.text = AppMessages.translateCurrent("settings.title")

        parserTypeSelect.setItems(roundParsers)
        parserTypeSelect.value = appSettings.selectedParser

        refreshParserTypeLabels()
        updateParserDescription(appSettings.selectedParser)

        parserTypeSelect.addValueChangeListener { event ->
            appSettings.selectedParser = event.value
            updateParserDescription(event.value)
        }

        maxRoundsField.isStepButtonsVisible = true
        maxRoundsField.min = 1
        maxRoundsField.value = appSettings.maxRounds
        maxRoundsField.addValueChangeListener { event ->
            val value = event.value ?: appSettings.maxRounds
            appSettings.maxRounds = value
            if (value < 1) {
                maxRoundsField.value = appSettings.maxRounds
            }
        }

        defaultHomeCourseField.value = appSettings.defaultHomeCourse.orEmpty()
        defaultHomeCourseField.addValueChangeListener { event ->
            appSettings.defaultHomeCourse = event.value
        }

        refreshMaxRoundsFieldLabels()
        refreshDefaultHomeCourseFieldLabels()

        add(title, parserTypeSelect, parserTypeDescription, maxRoundsField, defaultHomeCourseField)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "settings.title")
        refreshParserTypeLabels()
        updateParserDescription(parserTypeSelect.value)
        refreshMaxRoundsFieldLabels()
        refreshDefaultHomeCourseFieldLabels()
    }

    private fun refreshParserTypeLabels() {
        parserTypeSelect.label = AppMessages.translateCurrent("settings.parser.type.label")
        parserTypeSelect.setItemLabelGenerator { parser ->
            parser.javaClass.getAnnotation(ParserDefinition::class.java)
                ?.displayNameKey
                ?.let { AppMessages.translateCurrent(it) }
                ?: parser.javaClass.simpleName
        }
    }

    private fun updateParserDescription(parser: RoundParser?) {
        parserTypeDescription.text = parser
            ?.javaClass?.getAnnotation(ParserDefinition::class.java)
            ?.descriptionKey
            ?.let { AppMessages.translateCurrent(it) }
            ?: ""
    }

    private fun refreshDefaultHomeCourseFieldLabels() {
        defaultHomeCourseField.label = AppMessages.translateCurrent("settings.defaultHomeCourse.label")
        defaultHomeCourseField.placeholder = AppMessages.translateCurrent("settings.defaultHomeCourse.placeholder")
    }

    private fun refreshMaxRoundsFieldLabels() {
        maxRoundsField.label = AppMessages.translateCurrent("settings.maxRounds.label")
        maxRoundsField.helperText = AppMessages.translateCurrent("settings.maxRounds.helper")
    }
}
