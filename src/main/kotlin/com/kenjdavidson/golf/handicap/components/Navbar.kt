package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.views.AboutView
import com.kenjdavidson.golf.handicap.views.MainView
import com.kenjdavidson.golf.handicap.views.SettingsView
import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
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
import com.vaadin.flow.shared.Registration
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.Locale

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class Navbar(
    private val authenticationContext: AuthenticationContext,
    userProfileResolver: UserProfileResolver
) : HorizontalLayout(), LocaleChangeObserver {
    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val userProfile = userProfileResolver.buildUserProfile(authenticatedUser)
    private val icon = H2("⛳").apply {
        addClassNames(LumoUtility.Margin.Bottom.XSMALL)
        style["cursor"] = "pointer"
        element.setAttribute("role", "button")
        addClickListener {
            UI.getCurrent()?.navigate(MainView::class.java)
        }
    }
    private val heading = H2(AppMessages.translateCurrent("app.title")).apply {
        addClassNames(LumoUtility.Margin.Bottom.XSMALL)
        style["cursor"] = "pointer"
        element.setAttribute("role", "button")
        addClickListener {
            UI.getCurrent()?.navigate(MainView::class.java)
        }
    }
    private val avatar = Avatar(userProfile.displayName).apply {
        abbreviation = userProfile.initials
        style["cursor"] = "pointer"
        element.setAttribute("role", "button")
    }
    private val appMenuButton = Button(VaadinIcon.GRID_BIG.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY)
        style["width"] = "var(--lumo-size-m)"
        style["height"] = "var(--lumo-size-m)"
        style["min-width"] = "var(--lumo-size-m)"
        style["cursor"] = "pointer"
        element.setAttribute("role", "button")
        addClickListener {
            fireEvent(AppMenuToggleEvent(this@Navbar, true))
        }
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
        add(buildTitleSection(), buildUserSection())

        setWidthFull()
        isPadding = true
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        initializeSavedLocale()
        refreshLocalizedText(AppMessages.resolveLocale())
    }

    private fun buildTitleSection(): HorizontalLayout {
        return HorizontalLayout(icon, heading).apply {
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }
    }

    private fun buildUserSection(): HorizontalLayout {
        styleProfileMenuItem(userMenu.addItem(userMenuInfo))
        userMenu.addSeparator()

        return HorizontalLayout(appMenuButton, avatar).apply {
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

            style["margin-left"] = "auto"
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
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
        heading.text = AppMessages.translate(locale, "app.title")
        appMenuButton.element.setAttribute("aria-label", AppMessages.translate(locale, "menu.openMainMenu"))
        avatar.element.setAttribute("aria-label", AppMessages.translate(locale, "menu.openUserMenu"))
        memberNumber.text = buildMemberNumber(locale)
        refreshUserMenu(locale)
    }

    private fun refreshUserMenu(locale: Locale) {
        userMenu.removeAll()
        styleProfileMenuItem(userMenu.addItem(userMenuInfo))
        userMenu.addSeparator()
        styleMenuItem(userMenu.addItem(AppMessages.translate(locale, "menu.settings")) {
            UI.getCurrent()?.navigate(SettingsView::class.java)
        })

        val languageItem = userMenu.addItem(AppMessages.translate(locale, "menu.language"))
        styleMenuItem(languageItem)
        val currentLanguage = AppMessages.normalizeLocale(locale).language
        styleMenuItem(languageItem.subMenu.addItem(buildLanguageLabel(locale, Locale.ENGLISH, currentLanguage)) {
            applyLanguage(Locale.ENGLISH)
        })
        styleMenuItem(languageItem.subMenu.addItem(buildLanguageLabel(locale, Locale.FRENCH, currentLanguage)) {
            applyLanguage(Locale.FRENCH)
        })
        userMenu.addSeparator()
        styleMenuItem(userMenu.addItem(AppMessages.translate(locale, "menu.about")) {
            UI.getCurrent()?.navigate(AboutView::class.java)
        })
        userMenu.addSeparator()
        styleMenuItem(userMenu.addItem(AppMessages.translate(locale, "menu.logout")) {
            authenticationContext.logout()
        })
    }

    private fun buildMemberNumber(locale: Locale): String {
        val memberNumberText = authenticatedUser.golfCanadaCardId
            ?.takeIf { it.isNotBlank() }
            ?: AppMessages.translate(locale, "member.notAvailable")
        return AppMessages.translate(locale, "member.number", memberNumberText)
    }

    private fun styleProfileMenuItem(menuItem: MenuItem) {
        styleMenuItem(menuItem)
        menuItem.isEnabled = false
        menuItem.style["color"] = "var(--lumo-secondary-text-color)"
        menuItem.style["font-weight"] = "600"
        menuItem.style["font-size"] = "var(--lumo-font-size-s)"
        menuItem.style["opacity"] = "1"
    }

    private fun styleMenuItem(menuItem: MenuItem) {
        menuItem.style["min-width"] = "200px"
        menuItem.style["width"] = "200px"
    }

    private fun buildLanguageLabel(locale: Locale, optionLocale: Locale, currentLanguage: String): String {
        val labelKey = when (optionLocale.language) {
            Locale.FRENCH.language -> "menu.language.fr"
            else -> "menu.language.en"
        }
        val label = AppMessages.translate(locale, labelKey)
        return if (optionLocale.language == currentLanguage) "✓ $label" else label
    }

    fun addAppMenuToggleListener(listener: ComponentEventListener<AppMenuToggleEvent>): Registration {
        return addListener(AppMenuToggleEvent::class.java, listener)
    }

    class AppMenuToggleEvent(source: Navbar, fromClient: Boolean) : ComponentEvent<Navbar>(source, fromClient)
}
