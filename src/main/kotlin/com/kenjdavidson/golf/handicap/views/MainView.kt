package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
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
        val mainPanel = VerticalLayout(buildPathToolbar(), buildDashboardContent()).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            style["overflow"] = "auto"
            style["padding-bottom"] = "var(--lumo-space-l)"
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
        }
    }

    private fun buildDashboardContent(): Component {
        val heading = H2("Welcome to the Handicap Committee App").apply {
            addClassName(LumoUtility.Margin.Bottom.SMALL)
        }

        val description = Span(
            "Use the committee dashboard to access the current workspace and review " +
                "the application modules that will be expanded in future updates."
        ).apply {
            addClassName(LumoUtility.TextColor.SECONDARY)
        }

        val cards = Div(
            singleFileVerificationCardFactory.create(authenticatedUser),
            buildWelcomeCard("📄 PDF Parser", "Import and parse member round PDFs exported from Golf Canada."),
            buildWelcomeCard("🔍 Audit Log", "Review all changes made during this session."),
            buildWelcomeCard("👤 Member Rounds", "Browse individual member round history for the current season."),
            buildWelcomeCard("⚙️ Settings", "Configure Golf Canada API connection and display preferences.")
        ).apply {
            style["display"] = "grid"
            style["grid-template-columns"] = "repeat(auto-fill, minmax(280px, 1fr))"
            style["gap"] = "var(--lumo-space-m)"
            style["margin-top"] = "var(--lumo-space-l)"
        }

        return VerticalLayout(heading, description, cards).apply {
            addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.MaxWidth.SCREEN_LARGE
            )
            isPadding = true
            isSpacing = true
        }
    }

    private fun buildPathToolbar(): Component {
        val label = H4("Workspace Folder").apply {
            addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.SMALL
            )
        }

        val filePath = TextField().apply {
            isReadOnly = true
            setWidthFull()
            value = "No folder selected"
            prefixComponent = VaadinIcon.FOLDER_OPEN.create()
        }

        val selectFolder = Button("Select folder", VaadinIcon.FOLDER_OPEN_O.create())

        val controls = HorizontalLayout(filePath, selectFolder).apply {
            setWidthFull()
            defaultVerticalComponentAlignment = FlexComponent.Alignment.END
            expand(filePath)
        }

        return VerticalLayout(label, controls).apply {
            setWidthFull()
            isSpacing = false
            isPadding = true
            style["margin"] = "var(--lumo-space-m)"
            style["border-radius"] = "var(--lumo-border-radius-l)"
            style["background"] = "var(--lumo-contrast-5pct)"
            style["box-shadow"] = "var(--lumo-box-shadow-xs)"
        }
    }

    private fun buildWelcomeCard(title: String, subtitle: String): Div {
        val cardTitle = Span(title).apply {
            addClassNames(
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.FontWeight.SEMIBOLD
            )
        }

        val cardSubtitle = Span(subtitle).apply {
            addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
            )
        }

        return Div(cardTitle, cardSubtitle).apply {
            style["display"] = "flex"
            style["flex-direction"] = "column"
            style["gap"] = "var(--lumo-space-xs)"
            style["padding"] = "var(--lumo-space-m)"
            style["background"] = "var(--lumo-base-color)"
            style["border"] = "1px solid var(--lumo-contrast-10pct)"
            style["border-radius"] = "var(--lumo-border-radius-m)"
            style["box-shadow"] = "var(--lumo-box-shadow-xs)"
        }
    }

    private fun buildMenuPanel(): Component {
        val items = VerticalLayout(
            buildMenuButton("Dashboard"),
            buildMenuButton("Workspace"),
            buildMenuButton("Settings")
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
            style["position"] = "sticky"
            style["top"] = "0"
            style["align-self"] = "flex-start"
        }
    }

    private fun buildMenuButton(label: String): Button {
        return Button(label).apply {
            addThemeName("tertiary-inline")
            style["justify-content"] = "flex-start"
            setWidthFull()
            isEnabled = false
            element.setAttribute("aria-disabled", "true")
        }
    }

}
