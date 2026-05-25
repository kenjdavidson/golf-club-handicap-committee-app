package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.theme.lumo.Lumo
import jakarta.annotation.security.PermitAll

@PermitAll
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("context://styles/global.css")
class AuthenticatedView(
    navbar: Navbar,
    statusBar: StatusBar
) : AppLayout(), LocaleChangeObserver {
    private val viewContainer = Div().apply {
        setWidthFull()
    }

    init {
        addToNavbar(
            VerticalLayout(navbar, statusBar).apply {
                setWidthFull()
                isPadding = false
                isSpacing = false
                defaultHorizontalComponentAlignment = FlexComponent.Alignment.STRETCH
            }
        )

        content = VerticalLayout(viewContainer).apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        viewContainer.removeAll()
        if (content is Component) {
            viewContainer.add(content)
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {}
}
