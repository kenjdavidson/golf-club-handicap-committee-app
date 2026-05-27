package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.theme.lumo.LumoUtility
import java.time.format.DateTimeFormatter

class VerificationSummaryCard(result: FileVerificationResult) : VerticalLayout() {

    init {
        val statusColor = when (result.status) {
            VerificationStatus.PASS -> "var(--lumo-success-color)"
            VerificationStatus.WARNING -> "var(--lumo-warning-color)"
            VerificationStatus.ALERT -> "var(--lumo-error-color)"
        }
        val statusBadge = Span(result.status.name).apply {
            style["color"] = statusColor
            style["font-weight"] = "600"
            addClassNames(LumoUtility.FontSize.LARGE)
        }

        val matchRow = buildLabelValue(
            AppMessages.translateCurrent("main.result.dateMatch"),
            "${result.matchPercentage}% (${result.matchedCount}/${result.comparedCount})"
        )

        val mismatchText = if (result.mismatchedDates.isEmpty()) {
            AppMessages.translateCurrent("main.result.none")
        } else {
            result.mismatchedDates.joinToString(", ") { it.format(DATE_FORMATTER) }
        }
        val mismatchRow = buildLabelValue(
            AppMessages.translateCurrent("main.result.mismatchedDates"),
            mismatchText
        )

        val content = VerticalLayout(statusBadge, matchRow, mismatchRow).apply {
            isPadding = false
            isSpacing = true
        }

        if (result.notes.isNotEmpty()) {
            content.add(Paragraph(result.notes.joinToString("; ")).apply {
                addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY)
            })
        }

        isPadding = true
        isSpacing = true
        setWidthFull()
        style["background"] = "var(--lumo-base-color)"
        style["border"] = "1px solid var(--lumo-contrast-10pct)"
        style["border-radius"] = "var(--lumo-border-radius-m)"

        add(H4(AppMessages.translateCurrent("main.result.status")).apply {
            addClassNames(LumoUtility.Margin.NONE)
        })
        add(content)
    }

    private fun buildLabelValue(label: String, value: String): HorizontalLayout {
        val labelSpan = Span("$label: ").apply {
            addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.BOLD)
        }
        val valueSpan = Span(value).apply {
            addClassNames(LumoUtility.FontSize.SMALL)
        }
        return HorizontalLayout(labelSpan, valueSpan).apply {
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE
        }
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
