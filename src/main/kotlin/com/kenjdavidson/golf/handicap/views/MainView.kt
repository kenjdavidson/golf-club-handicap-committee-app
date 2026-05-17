package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.menubar.MenuBar
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
    private val authenticationContext: AuthenticationContext
) : AppLayout() {

    private val userProfile = resolveUserProfile()

    init {
        addToNavbar(buildNavbar())
        content = buildMainContent()
    }

    private fun buildNavbar(): Component {
        val title = H2("Golf Club Handicap Committee").apply {
            addClassNames(
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.Margin.NONE
            )
        }

        val icon = Span("⛳").apply {
            addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.Right.SMALL
            )
        }

        val brand = HorizontalLayout(icon, title).apply {
            isSpacing = false
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }

        return HorizontalLayout(brand, buildUserSection()).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            setWidthFull()
            expand(brand)
            addClassNames(LumoUtility.Padding.Horizontal.MEDIUM)
        }
    }

    private fun buildUserSection(): Component {
        val menuBar = MenuBar().apply {
            element.setAttribute("aria-label", "User menu")
        }
        val menuItem: MenuItem = menuBar.addItem(VaadinIcon.CHEVRON_DOWN_SMALL.create()).apply {
            element.setAttribute("aria-label", "Open user menu")
            subMenu.addItem("Log out") { authenticationContext.logout() }
        }

        val avatar = Avatar(userProfile.displayName).apply {
            abbreviation = userProfile.initials
        }

        val name = Span(userProfile.displayName).apply {
            addClassNames(LumoUtility.FontWeight.MEDIUM)
        }

        val details = Span(userProfile.details).apply {
            addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
            )
        }

        val info = VerticalLayout(name, details).apply {
            isPadding = false
            isSpacing = false
            setMargin(false)
        }

        return HorizontalLayout(menuBar, avatar, info).apply {
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }
    }

    private fun buildMainContent(): Component {
        val content = buildDashboardContent()
        val wrapper = VerticalLayout(buildPathToolbar(), content, buildStatusBar()).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            expand(content)
        }

        return HorizontalLayout(wrapper).apply {
            setSizeFull()
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

    private fun buildStatusBar(): Component {
        val leftStatus = Span("Status: Ready").apply {
            addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
            )
        }

        val rightStatus = Span("Authentication active with Golf Canada").apply {
            addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
            )
        }

        return HorizontalLayout(leftStatus, rightStatus).apply {
            setWidthFull()
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            expand(leftStatus)
            addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL
            )
            style["border-top"] = "1px solid var(--lumo-contrast-10pct)"
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

    private fun resolveUserProfile(): UserProfile =
        authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser::class.java)
            .map { authenticatedUser ->
                UserProfile(
                    displayName = authenticatedUser.displayName,
                    details = buildUserDetails(authenticatedUser),
                    initials = buildInitials(authenticatedUser.displayName)
                )
            }
            .orElseThrow { IllegalStateException("Authenticated Golf Canada user not available.") }

    private fun buildUserDetails(authenticatedUser: GolfCanadaAuthenticatedUser): String =
        listOfNotNull(
            authenticatedUser.email,
            authenticatedUser.handicap?.takeIf { it.isNotBlank() }?.let { "HCP $it" },
            authenticatedUser.membershipLevel?.takeIf { it.isNotBlank() }
        ).joinToString(" • ")

    private fun buildInitials(displayName: String?): String {
        if (displayName.isNullOrBlank()) {
            return "CU"
        }

        val parts = displayName.trim().split("\\s+".toRegex())
        val first = parts.firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        val second = parts.drop(1).lastOrNull()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return first + second
    }

    private data class UserProfile(
        val displayName: String,
        val details: String,
        val initials: String
    )
}
