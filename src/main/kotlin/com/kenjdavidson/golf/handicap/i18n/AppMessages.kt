package com.kenjdavidson.golf.handicap.i18n

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinSession
import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle

object AppMessages {
    const val SESSION_LANGUAGE_KEY = "preferredLanguage"

    val supportedLocales: List<Locale> = listOf(Locale.ENGLISH, Locale.FRENCH)

    fun resolveLocale(): Locale {
        val sessionLanguage = VaadinSession.getCurrent()
            ?.getAttribute(SESSION_LANGUAGE_KEY) as? String
        val sessionLocale = sessionLanguage?.let(::localeForLanguage)
        return normalizeLocale(UI.getCurrent()?.locale ?: sessionLocale ?: VaadinSession.getCurrent()?.locale)
    }

    fun normalizeLocale(locale: Locale?): Locale {
        val language = locale?.language?.lowercase(Locale.ROOT)
        return when (language) {
            Locale.FRENCH.language -> Locale.FRENCH
            else -> Locale.ENGLISH
        }
    }

    fun localeForLanguage(language: String?): Locale? {
        return when (language?.lowercase(Locale.ROOT)) {
            Locale.ENGLISH.language -> Locale.ENGLISH
            Locale.FRENCH.language -> Locale.FRENCH
            else -> null
        }
    }

    fun translate(locale: Locale?, key: String, vararg params: Any?): String {
        val resolvedLocale = normalizeLocale(locale)
        val pattern = ResourceBundle.getBundle(BUNDLE_BASE_NAME, resolvedLocale).getString(key)
        return if (params.isEmpty()) {
            pattern
        } else {
            MessageFormat(pattern, resolvedLocale).format(params)
        }
    }

    fun translateCurrent(key: String, vararg params: Any?): String {
        return translate(resolveLocale(), key, *params)
    }

    private const val BUNDLE_BASE_NAME = "i18n/messages"
}
