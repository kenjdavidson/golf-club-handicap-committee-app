package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll

@Route("")
@PageTitle("Handicap Committee App")
@PermitAll
class MainView(
    private val authenticationContext: AuthenticationContext,
    private val userProfileResolver: UserProfileResolver,
    private val singleFileVerificationCardFactory: SingleFileVerificationCardFactory,
    private val navbar: Navbar,
    private val statusBar: StatusBar
) : AppLayout() {

    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val menuPanel = buildMenuPanel()

    init {
        navbar.setMenuToggleListener { menuPanel.isVisible = !menuPanel.isVisible }
        addToNavbar(navbar)
        content = buildMainContent()
    }

    private fun buildMainContent(): Component {
        val mainPanel = VerticalLayout(singleFileVerificationCardFactory.create(authenticatedUser)).apply {
            setSizeFull()
            isPadding = true
            isSpacing = true
            style["overflow"] = "auto"
            style["min-height"] = "0"
            element.setAttribute("tabindex", "0")
            element.setAttribute("aria-label", "Main content")
        }

        val body = HorizontalLayout(menuPanel, mainPanel).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            expand(mainPanel)
        }

        return VerticalLayout(body, statusBar).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            expand(body)
            style["overflow"] = "hidden"
        }
    }

    private fun buildMenuPanel(): Component {
        val items = VerticalLayout(
            buildMenuLink("Lookup", MainView::class.java),
            buildMenuLink("Settings", SettingsView::class.java)
        ).apply {
            isPadding = false
            isSpacing = false
            isMargin = false
            setWidthFull()
        }

        return Div(items).apply {
            style["width"] = "220px"
            style["padding"] = "var(--lumo-space-m)"
            style["border-right"] = "1px solid var(--lumo-contrast-10pct)"
            style["background"] = "var(--lumo-base-color)"
            style["height"] = "100%"
            style["flex-shrink"] = "0"
        }
    }

    private fun buildMenuLink(label: String, route: Class<out Component>): RouterLink {
        return RouterLink(label, route).apply {
            style["display"] = "block"
            style["padding"] = "var(--lumo-space-s) 0"
            style["color"] = "var(--lumo-body-text-color)"
            style["text-decoration"] = "none"
            style["width"] = "100%"
        }
    }

}
