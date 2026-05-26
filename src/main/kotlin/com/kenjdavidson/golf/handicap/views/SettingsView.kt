package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.settings.AppSettings
import com.kenjdavidson.golf.handicap.verification.ParserType
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
    private val appSettings: AppSettings
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()

    private val parserTypeSelect = Select<ParserType>().apply {
        setItems(*ParserType.entries.toTypedArray())
        value = appSettings.selectedParserType
    }

    private val parserTypeDescription = Paragraph()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        title.text = AppMessages.translateCurrent("settings.title")

        refreshParserTypeLabels()
        updateParserTypeDescription(appSettings.selectedParserType)

        parserTypeSelect.addValueChangeListener { event ->
            appSettings.selectedParserType = event.value
            updateParserTypeDescription(event.value)
        }

        add(title, parserTypeSelect, parserTypeDescription)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "settings.title")
        refreshParserTypeLabels()
        updateParserTypeDescription(parserTypeSelect.value)
    }

    private fun refreshParserTypeLabels() {
        parserTypeSelect.label = AppMessages.translateCurrent("settings.parser.type.label")
        parserTypeSelect.setItemLabelGenerator { type ->
            AppMessages.translateCurrent(type.displayNameKey)
        }
    }

    private fun updateParserTypeDescription(type: ParserType?) {
        parserTypeDescription.text = type?.let {
            AppMessages.translateCurrent(it.descriptionKey)
        } ?: ""
    }
}

