package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Locale

@Component
class AboutContentLoader {
    fun requireMarkdown(locale: Locale): String {
        val language = AppMessages.normalizeLocale(locale).language
        return readMarkdown("about/about_$language.md")
            ?: readMarkdown("about/about_en.md")
            ?: throw IllegalStateException("Missing about markdown resources")
    }

    private fun readMarkdown(resourcePath: String): String? {
        val resource = ClassPathResource(resourcePath)
        if (!resource.exists()) {
            return null
        }
        return resource.inputStream.use { input ->
            String(input.readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}
