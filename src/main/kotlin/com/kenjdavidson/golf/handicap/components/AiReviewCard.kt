package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.theme.lumo.LumoUtility

/**
 * Card component that displays the AI handicap review result.
 *
 * Transitions through three visual states:
 * 1. **Loading** – shows an indeterminate spinner and a "please wait" label.
 * 2. **Result** – renders the AI-generated review text. A single summary line is
 *    shown prominently; any lines starting with "-" are rendered as a bullet list.
 * 3. **Error** – shows a brief error message.
 *
 * Call [showResult] or [showError] from a Vaadin `ui.access {}` block after the
 * background AI call completes.
 */
class AiReviewCard : VerticalLayout() {

    private val loadingBar = ProgressBar().apply { isIndeterminate = true }
    private val loadingLabel = Span().apply {
        addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY)
    }
    private val contentArea = VerticalLayout().apply {
        isPadding = false
        isSpacing = true
        isVisible = false
    }

    init {
        isPadding = true
        isSpacing = true
        setWidthFull()
        style["background"] = "var(--lumo-base-color)"
        style["border"] = "1px solid var(--lumo-contrast-10pct)"
        style["border-radius"] = "var(--lumo-border-radius-m)"

        loadingLabel.text = AppMessages.translateCurrent("ai.review.loading")

        add(
            H4(AppMessages.translateCurrent("ai.review.title")).apply {
                addClassNames(LumoUtility.Margin.NONE)
            },
            loadingBar,
            loadingLabel,
            contentArea
        )
    }

    /** Replaces the loading indicator with the AI-generated [reviewText]. */
    fun showResult(reviewText: String) {
        loadingBar.isVisible = false
        loadingLabel.isVisible = false
        contentArea.removeAll()

        val lines = reviewText.trim().lines()
        val summaryLine = lines.firstOrNull { it.isNotBlank() }
        val bulletLines = lines.drop(if (summaryLine != null) 1 else 0)
            .filter { it.trim().startsWith("-") }

        if (summaryLine != null) {
            contentArea.add(Paragraph(summaryLine).apply {
                addClassNames(LumoUtility.FontWeight.BOLD)
            })
        }

        if (bulletLines.isNotEmpty()) {
            val list = UnorderedList().apply {
                addClassNames(LumoUtility.Margin.NONE)
            }
            bulletLines.forEach { line ->
                list.add(ListItem(line.trimStart('-', ' ')))
            }
            contentArea.add(list)
        }

        contentArea.isVisible = true
    }

    /** Replaces the loading indicator with an error message. */
    fun showError(message: String) {
        loadingBar.isVisible = false
        loadingLabel.isVisible = false
        contentArea.removeAll()
        contentArea.add(Span(AppMessages.translateCurrent("ai.review.error", message)).apply {
            addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL)
        })
        contentArea.isVisible = true
    }
}
