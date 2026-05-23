package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.RoundComparison
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.theme.lumo.LumoUtility
import java.time.format.DateTimeFormatter

class RoundsComparisonGrid(comparisons: List<RoundComparison>) : VerticalLayout() {

    init {
        val grid = Grid<RoundComparison>().apply {
            setWidthFull()
            isAllRowsVisible = true

            addColumn { it.pdfRound.playedDate.format(DATE_FORMATTER) }
                .setHeader(AppMessages.translateCurrent("main.rounds.date"))
                .setFlexGrow(0).setWidth("110px")

            addColumn { it.pdfRound.playDistance ?: "" }
                .setHeader(AppMessages.translateCurrent("main.rounds.course"))
                .setFlexGrow(1)

            addColumn { it.pdfRound.playingPartners.joinToString(", ") }
                .setHeader(AppMessages.translateCurrent("main.rounds.playingWith"))
                .setFlexGrow(1)

            addColumn { it.golfCanadaEntry?.course ?: "" }
                .setHeader(AppMessages.translateCurrent("main.rounds.gcCourse"))
                .setFlexGrow(1)

            addColumn { comp ->
                comp.golfCanadaEntry?.score?.toString() ?: ""
            }.setHeader(AppMessages.translateCurrent("main.rounds.gcScore"))
                .setFlexGrow(0).setWidth("80px")

            addColumn { comp ->
                comp.golfCanadaEntry?.differential?.let { "%.1f".format(it) } ?: ""
            }.setHeader(AppMessages.translateCurrent("main.rounds.gcDifferential"))
                .setFlexGrow(0).setWidth("100px")

            addComponentColumn { comp ->
                val matched = comp.isMatched
                Span(
                    if (matched) AppMessages.translateCurrent("main.rounds.matched")
                    else AppMessages.translateCurrent("main.rounds.unmatched")
                ).apply {
                    addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.BOLD)
                    style["color"] = if (matched) "var(--lumo-success-color)" else "var(--lumo-error-color)"
                }
            }.setHeader(AppMessages.translateCurrent("main.result.status"))
                .setFlexGrow(0).setWidth("110px")

            setItems(comparisons)
            classNames.add("rounds-comparison-grid")
        }

        val title = H4(
            AppMessages.translateCurrent("main.rounds.title", comparisons.size.toString())
        ).apply {
            addClassNames(LumoUtility.Margin.NONE, LumoUtility.Margin.Bottom.SMALL)
        }

        setWidthFull()
        isPadding = true
        isSpacing = true
        style["background"] = "var(--lumo-base-color)"
        style["border"] = "1px solid var(--lumo-contrast-10pct)"
        style["border-radius"] = "var(--lumo-border-radius-m)"
        style["margin-top"] = "var(--lumo-space-m)"

        add(title, grid)
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
