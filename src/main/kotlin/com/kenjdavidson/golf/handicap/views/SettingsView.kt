package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "settings", layout = AuthenticatedView::class)
@PageTitle("Settings | Handicap Committee App")
@PermitAll
class SettingsView : VerticalLayout() {
    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        add(H2("Settings"))
    }
}
