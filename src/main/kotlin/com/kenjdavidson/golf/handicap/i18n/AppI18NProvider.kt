package com.kenjdavidson.golf.handicap.i18n

import com.vaadin.flow.i18n.I18NProvider
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class AppI18NProvider : I18NProvider {
    override fun getProvidedLocales(): List<Locale> = AppMessages.supportedLocales

    override fun getTranslation(key: String, locale: Locale?, vararg params: Any?): String {
        return AppMessages.translate(locale, key, *params)
    }
}
