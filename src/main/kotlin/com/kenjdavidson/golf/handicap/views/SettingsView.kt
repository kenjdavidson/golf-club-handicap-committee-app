package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.settings.AppSettings
import com.kenjdavidson.golf.handicap.verification.ParserDefinition
import com.kenjdavidson.golf.handicap.verification.RoundParser
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "settings", layout = AuthenticatedView::class)
@PageTitle("Settings | Handicap Committee App")
@PermitAll
class SettingsView(
    private val appSettings: AppSettings,
    private val roundParsers: List<RoundParser>
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()
    private val parserTypeSelect = Select<RoundParser>()
    private val parserTypeDescription = Paragraph()

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

        add(title, parserTypeSelect, parserTypeDescription)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "settings.title")
        refreshParserTypeLabels()
        updateParserDescription(parserTypeSelect.value)
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
}

