package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.markdown.Markdown
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "about", layout = AuthenticatedView::class)
@PageTitle("About | Handicap Committee App")
@PermitAll
class AboutView(
    private val aboutContentLoader: AboutContentLoader
) : VerticalLayout(), LocaleChangeObserver {
    private val title = H2()
    private val markdown = Markdown()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
        title.text = AppMessages.translateCurrent("about.title")
        markdown.content = aboutContentLoader.requireMarkdown(AppMessages.resolveLocale())
        add(title, markdown)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "about.title")
        markdown.content = aboutContentLoader.requireMarkdown(event.locale)
    }
}
