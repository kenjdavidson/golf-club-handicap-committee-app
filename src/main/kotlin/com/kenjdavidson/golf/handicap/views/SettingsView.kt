package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "settings", layout = AuthenticatedView::class)
@PageTitle("Settings | Handicap Committee App")
@PermitAll
class SettingsView : VerticalLayout(), LocaleChangeObserver {
    private val title = H2()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        title.text = AppMessages.translateCurrent("settings.title")
        add(title)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "settings.title")
    }
}
