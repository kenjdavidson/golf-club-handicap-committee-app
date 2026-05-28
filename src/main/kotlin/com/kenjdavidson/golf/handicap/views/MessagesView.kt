package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.LoggingMessageService
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Pre
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Route(value = "messages", layout = AuthenticatedView::class)
@PageTitle("Messages | Handicap Committee App")
@PermitAll
class MessagesView(
    private val loggingMessageService: LoggingMessageService
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()
    private val clearButton = Button().apply {
        addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
    }
    private val messageList = VerticalLayout().apply {
        isPadding = false
        isSpacing = true
        setWidthFull()
    }

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        clearButton.addClickListener {
            loggingMessageService.clearMessages()
            refreshMessages()
        }

        val header = HorizontalLayout(title, clearButton).apply {
            setWidthFull()
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            expand(title)
        }

        add(header, messageList)
        refreshLocalizedText(AppMessages.resolveLocale())
        refreshMessages()
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
    }

    private fun refreshLocalizedText(locale: java.util.Locale) {
        title.text = AppMessages.translate(locale, "messages.title")
        clearButton.text = AppMessages.translate(locale, "messages.clearAll")
    }

    private fun refreshMessages() {
        messageList.removeAll()
        val messages = loggingMessageService.getMessages()
        if (messages.isEmpty()) {
            messageList.add(Paragraph(AppMessages.translateCurrent("messages.empty")))
            clearButton.isEnabled = false
        } else {
            clearButton.isEnabled = true
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(AppMessages.resolveLocale())
                .withZone(ZoneId.systemDefault())
            messages.forEach { logMessage ->
                val timestamp = Span(formatter.format(logMessage.timestamp)).apply {
                    style["color"] = "var(--lumo-secondary-text-color)"
                    style["font-size"] = "var(--lumo-font-size-s)"
                }
                val messageText = H4(logMessage.message)
                val card = VerticalLayout(timestamp, messageText).apply {
                    setWidthFull()
                    isPadding = true
                    isSpacing = false
                    style["border"] = "1px solid var(--lumo-error-color-10pct)"
                    style["border-radius"] = "var(--lumo-border-radius-m)"
                    style["background"] = "var(--lumo-error-color-10pct)"
                }
                if (logMessage.stackTrace != null) {
                    val stackTraceContent = Pre(logMessage.stackTrace).apply {
                        style["white-space"] = "pre-wrap"
                        style["word-break"] = "break-word"
                        style["font-size"] = "var(--lumo-font-size-xs)"
                        style["max-height"] = "300px"
                        style["overflow"] = "auto"
                    }
                    val details = Details(
                        AppMessages.translateCurrent("messages.stackTrace"),
                        stackTraceContent
                    )
                    card.add(details)
                }
                messageList.add(card)
            }
        }
    }
}
