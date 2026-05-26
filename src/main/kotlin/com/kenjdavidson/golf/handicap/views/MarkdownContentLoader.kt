package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Locale

@Component
class MarkdownContentLoader {
    fun requireLocalizedMarkdown(
        resourcePrefix: String,
        locale: Locale,
        fallbackLanguage: String = Locale.ENGLISH.language
    ): String {
        val language = AppMessages.normalizeLocale(locale).language
        return readMarkdown("${resourcePrefix}_$language.md")
            ?: readMarkdown("${resourcePrefix}_$fallbackLanguage.md")
            ?: throw IllegalStateException("Missing markdown resources for prefix '$resourcePrefix'")
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
