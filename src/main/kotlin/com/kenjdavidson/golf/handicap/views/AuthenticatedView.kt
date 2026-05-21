package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.RouterLink
import jakarta.annotation.security.PermitAll

@PermitAll
@StyleSheet("context://styles/global.css")
class AuthenticatedView(
    navbar: Navbar,
    statusBar: StatusBar
) : AppLayout() {
    private val viewContainer = Div().apply {
        setSizeFull()

        style["overflow"] = "auto"
        style["min-height"] = "0"
    }

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
        return Div(
            buildMenuLink("Lookup", MainView::class.java),
            buildMenuLink("Settings", SettingsView::class.java)
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

    private fun buildMenuLink(label: String, route: Class<out Component>): RouterLink {
        return RouterLink(label, route).apply {
            style["display"] = "block"
            style["padding"] = "var(--lumo-space-xs) 0"
            style["color"] = "var(--lumo-body-text-color)"
            style["text-decoration"] = "none"
        }
    }
}
