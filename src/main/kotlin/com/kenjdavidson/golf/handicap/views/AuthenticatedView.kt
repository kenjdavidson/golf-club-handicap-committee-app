package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.RouterLink
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
        setSizeFull()

        style["overflow"] = "auto"
        style["min-height"] = "0"
    }

    private lateinit var lookupLink: RouterLink
    private lateinit var settingsLink: RouterLink

    init {
        navbar.setMenuToggleListener { isDrawerOpened = !isDrawerOpened }
        addToNavbar(navbar)
        addToDrawer(buildMenuPanel())
        isDrawerOpened = true

        content = VerticalLayout(viewContainer, statusBar).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            expand(viewContainer)
            style["overflow"] = "hidden"
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        viewContainer.removeAll()
        if (content is Component) {
            content.element.style["height"] = "100%"
            viewContainer.add(content)
        }
    }

    private fun buildMenuPanel(): Div {
        lookupLink = buildMenuLink(MainView::class.java)
        settingsLink = buildMenuLink(SettingsView::class.java)
        refreshMenuLabels()
        return Div(
            lookupLink,
            settingsLink
        ).apply {
            style["width"] = "220px"
            style["padding"] = "var(--lumo-space-m)"
            style["height"] = "100%"
            style["box-sizing"] = "border-box"
            style["display"] = "flex"
            style["flex-direction"] = "column"
            style["gap"] = "var(--lumo-space-s)"
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshMenuLabels()
    }

    private fun buildMenuLink(route: Class<out Component>): RouterLink {
        return RouterLink("", route).apply {
            style["display"] = "block"
            style["padding"] = "var(--lumo-space-xs) 0"
            style["color"] = "var(--lumo-body-text-color)"
            style["text-decoration"] = "none"
        }
    }

    private fun refreshMenuLabels() {
        lookupLink.text = AppMessages.translateCurrent("menu.lookup")
        settingsLink.text = AppMessages.translateCurrent("menu.settings")
    }
}
