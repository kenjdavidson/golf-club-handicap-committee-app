package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.views.UserProfileResolver
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
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
    private var menuToggleListener: (() -> Unit)? = null

    init {
        val menuButton = Button(VaadinIcon.MENU.create()).apply {
            element.setAttribute("aria-label", "Toggle navigation menu")
            addClickListener { menuToggleListener?.invoke() }
        }

        val name = Span(userProfile.displayName).apply {
            addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.BOLD
            )
        }

        val memberNumber = Span(buildMemberNumber()).apply {
            addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
            )
        }

        val info = VerticalLayout(name, memberNumber).apply {
            isPadding = false
            isSpacing = false
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }

        add(menuButton, info, buildUserSection())
        setWidthFull()
        isPadding = true
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(info)
    }

    private fun buildUserSection(): HorizontalLayout {
        val avatar = Avatar(userProfile.displayName).apply {
            abbreviation = userProfile.initials
        }
        val avatarButton = Button(avatar).apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON)
            style["padding"] = "0"
            style["min-width"] = "0"
            style["border"] = "none"
            style["cursor"] = "pointer"
            element.setAttribute("aria-label", "Open user menu")
        }

        ContextMenu(avatarButton).apply {
            isOpenOnClick = true
            addItem("Logout") { authenticationContext.logout() }
        }

        return HorizontalLayout(avatarButton).apply {
            isSpacing = false
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }
    }

    private fun buildMemberNumber(): String {
        val memberNumberText = authenticatedUser.golfCanadaCardId?.takeIf { it.isNotBlank() } ?: "not available"
        return "Member #$memberNumberText"
    }

    fun setMenuToggleListener(listener: () -> Unit) {
        menuToggleListener = listener
    }
}
