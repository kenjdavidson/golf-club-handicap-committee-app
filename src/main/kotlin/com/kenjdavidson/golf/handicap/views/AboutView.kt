package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.MarkdownContentLoader
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.markdown.Markdown
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll
import org.springframework.beans.factory.annotation.Value

@Route(value = "about", layout = AuthenticatedView::class)
@PermitAll
class AboutView(
    private val markdownContentLoader: MarkdownContentLoader,
    @Value("\${app.ui.title:Golf Handicap App}") private val appTitle: String
) : VerticalLayout(), LocaleChangeObserver, HasDynamicTitle {
    private val title = H2()
    private val markdown = Markdown()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
        title.text = AppMessages.translateCurrent("about.title")
        markdown.content = markdownContentLoader.requireLocalizedMarkdown("about/about", AppMessages.resolveLocale())
        add(title, markdown)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "about.title")
        markdown.content = markdownContentLoader.requireLocalizedMarkdown("about/about", event.locale)
        UI.getCurrent()?.page?.setTitle(pageTitle(event.locale))
    }

    override fun getPageTitle(): String {
        return pageTitle(AppMessages.resolveLocale())
    }

    private fun pageTitle(locale: java.util.Locale): String {
        return "${AppMessages.translate(locale, "about.title")} | $appTitle"
    }
}
