package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.views.UserProfileResolver
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.Locale

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class Navbar(
    @Value("\${app.ui.title: Golf Handicap App}") appTitle: String,
    private val authenticationContext: AuthenticationContext,
    userProfileResolver: UserProfileResolver
) : HorizontalLayout(), LocaleChangeObserver {
    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val userProfile = userProfileResolver.buildUserProfile(authenticatedUser)
    private var menuToggleListener: (() -> Unit)? = null
    private val menuButton = Button(VaadinIcon.MENU.create())
    private val heading = H2("⛳ $appTitle").apply {
        addClassNames(LumoUtility.Margin.Bottom.XSMALL)
    }
    private val avatar = Avatar(userProfile.displayName).apply {
        abbreviation = userProfile.initials
        style["cursor"] = "pointer"
        element.setAttribute("role", "button")
    }
    private val name = Span(userProfile.displayName).apply {
        addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.FontWeight.BOLD
        )
    }
    private val memberNumber = Span().apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }
    private val userMenu = ContextMenu(avatar).apply {
        isOpenOnClick = true
    }
    private val userMenuInfo = VerticalLayout(name, memberNumber).apply {
        isPadding = false
        isSpacing = false
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
    }

    init {
        menuButton.addClickListener { menuToggleListener?.invoke() }

        add(menuButton, heading, buildUserSection())

        setWidthFull()
        isPadding = true
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        initializeSavedLocale()
        refreshLocalizedText(AppMessages.resolveLocale())
    }

    private fun buildUserSection(): HorizontalLayout {
        styleProfileMenuItem(userMenu.addItem(userMenuInfo))
        userMenu.addSeparator()

        return HorizontalLayout(avatar).apply {
            isSpacing = false
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

            style["margin-left"] = "auto"
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
    }

    fun setMenuToggleListener(listener: () -> Unit) {
        menuToggleListener = listener
    }

    private fun initializeSavedLocale() {
        val sessionLanguage = VaadinSession.getCurrent()?.getAttribute(AppMessages.SESSION_LANGUAGE_KEY) as? String
        if (sessionLanguage != null) {
            AppMessages.localeForLanguage(sessionLanguage)?.let { applyLanguage(it, persistToStorage = false) }
            return
        }

        UI.getCurrent()?.page
            ?.executeJs("return window.localStorage.getItem($0);", AppMessages.SESSION_LANGUAGE_KEY)
            ?.then(String::class.java) { language ->
                AppMessages.localeForLanguage(language)?.let { applyLanguage(it, persistToStorage = false) }
            }
    }

    private fun applyLanguage(locale: Locale, persistToStorage: Boolean = true) {
        val normalizedLocale = AppMessages.normalizeLocale(locale)
        UI.getCurrent()?.locale = normalizedLocale
        VaadinSession.getCurrent()?.locale = normalizedLocale
        VaadinSession.getCurrent()?.setAttribute(AppMessages.SESSION_LANGUAGE_KEY, normalizedLocale.language)
        if (persistToStorage) {
            UI.getCurrent()?.page
                ?.executeJs("window.localStorage.setItem($0, $1);", AppMessages.SESSION_LANGUAGE_KEY, normalizedLocale.language)
        }
        refreshLocalizedText(normalizedLocale)
    }

    private fun refreshLocalizedText(locale: Locale) {
        menuButton.element.setAttribute("aria-label", AppMessages.translate(locale, "menu.toggleNavigation"))
        avatar.element.setAttribute("aria-label", AppMessages.translate(locale, "menu.openUserMenu"))
        memberNumber.text = buildMemberNumber(locale)
        refreshUserMenu(locale)
    }

    private fun refreshUserMenu(locale: Locale) {
        userMenu.removeAll()
        styleProfileMenuItem(userMenu.addItem(userMenuInfo))
        userMenu.addSeparator()

        val languageItem = userMenu.addItem(AppMessages.translate(locale, "menu.language"))
        languageItem.subMenu.addItem(AppMessages.translate(locale, "menu.language.en")) {
            applyLanguage(Locale.ENGLISH)
        }
        languageItem.subMenu.addItem(AppMessages.translate(locale, "menu.language.fr")) {
            applyLanguage(Locale.FRENCH)
        }
        userMenu.addSeparator()
        userMenu.addItem(AppMessages.translate(locale, "menu.logout")) {
            authenticationContext.logout()
        }
    }

    private fun buildMemberNumber(locale: Locale): String {
        val memberNumberText = authenticatedUser.golfCanadaCardId
            ?.takeIf { it.isNotBlank() }
            ?: AppMessages.translate(locale, "member.notAvailable")
        return AppMessages.translate(locale, "member.number", memberNumberText)
    }

    private fun styleProfileMenuItem(menuItem: MenuItem) {
        menuItem.isEnabled = false
        menuItem.style["color"] = "var(--lumo-secondary-text-color)"
        menuItem.style["font-weight"] = "600"
        menuItem.style["font-size"] = "var(--lumo-font-size-s)"
        menuItem.style["opacity"] = "1"
    }
}
