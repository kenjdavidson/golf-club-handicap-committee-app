package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.views.UserProfileResolver
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class Navbar(
    private val authenticationContext: AuthenticationContext,
    userProfileResolver: UserProfileResolver
) : HorizontalLayout() {

    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val userProfile = userProfileResolver.buildUserProfile(authenticatedUser)

    init {
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

        val navButtons = HorizontalLayout(
            Button("Dashboard"),
            Button("Workspace")
        ).apply {
            isPadding = false
            isSpacing = true
            setMargin(false)
        }

        add(brand, navButtons, buildUserSection())
        setWidthFull()
        isPadding = true
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(brand)
    }

    private fun buildUserSection(): HorizontalLayout {
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
}
