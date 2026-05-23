package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.GolfCanadaMemberMatch
import com.kenjdavidson.golf.handicap.verification.ParsedRound
import com.kenjdavidson.golf.handicap.verification.RoundComparison
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import jakarta.annotation.security.PermitAll
import org.springframework.context.ApplicationEventPublisher
import java.time.format.DateTimeFormatter

@Route(value = "", layout = AuthenticatedView::class)
@PageTitle("Handicap Committee App")
@PermitAll
class MainView(
    private val authenticationContext: AuthenticationContext,
    private val userProfileResolver: UserProfileResolver,
    private val singleFileVerificationService: SingleFileVerificationService,
    private val eventPublisher: ApplicationEventPublisher
) : VerticalLayout(), LocaleChangeObserver {

    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val verificationResult = Div().apply {
        setWidthFull()
    }

    init {
        val uploadCard = SingleFileUploadCard()
        uploadCard.setFileSelectedListener { fileName ->
            publishStatus(AppMessages.translateCurrent("main.status.uploaded", fileName))
        }
        uploadCard.setFileRejectedListener { message ->
            publishStatus(message)
            verificationResult.removeAll()
        }
        uploadCard.setVerifyHandler { fileName, fileBytes ->
            verifyFile(fileName, fileBytes)
        }

        setSizeFull()
        isPadding = true
        isSpacing = true
        style["overflow"] = "auto"
        style["min-height"] = "0"
        element.setAttribute("tabindex", "0")
        element.setAttribute("aria-label", AppMessages.translateCurrent("main.aria.mainContent"))
        add(
            VerticalLayout(uploadCard, verificationResult).apply {
                setWidthFull()
                isPadding = true
                isSpacing = true
                style["box-sizing"] = "border-box"
                style["background"] = "var(--lumo-contrast-5pct)"
                style["border"] = "1px solid var(--lumo-contrast-10pct)"
                style["border-radius"] = "var(--lumo-border-radius-l)"
            }
        )
    }

    override fun localeChange(event: LocaleChangeEvent) {
        element.setAttribute("aria-label", AppMessages.translate(event.locale, "main.aria.mainContent"))
    }

    private fun verifyFile(fileName: String, fileBytes: ByteArray) {
        try {
            publishStatus(AppMessages.translateCurrent("main.status.verifying", fileName))
            val result = singleFileVerificationService.verify(fileName, fileBytes, authenticatedUser)
            renderResult(result)
            publishStatus(statusText(result.status))
        } catch (exception: VerificationProcessingException) {
            val message = exception.message ?: AppMessages.translateCurrent("main.status.failed")
            publishStatus(message)
            verificationResult.removeAll()
        }
    }

    private fun renderResult(result: FileVerificationResult) {
        verificationResult.removeAll()

        val topRow = HorizontalLayout().apply {
            setWidthFull()
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.START
        }

        topRow.add(buildMemberProfileCard(result.matchedMember, result.playerName, result.memberId))
        topRow.add(buildVerificationSummaryCard(result))
        topRow.expand(topRow.getComponentAt(1))

        verificationResult.add(topRow)

        if (result.roundComparisons.isNotEmpty()) {
            verificationResult.add(buildRoundsComparisonSection(result.roundComparisons))
        }
    }

    private fun buildMemberProfileCard(
        matchedMember: GolfCanadaMemberMatch?,
        playerName: String?,
        memberId: String?
    ): VerticalLayout {
        val initials = buildInitials(matchedMember?.fullName ?: playerName)
        val displayName = matchedMember?.fullName ?: playerName ?: AppMessages.translateCurrent("member.notAvailable")
        val avatar = Avatar(displayName).apply {
            abbreviation = initials
        }

        val nameSpan = Span(displayName).apply {
            addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.MEDIUM)
        }

        val idText = matchedMember?.golfCanadaCardId ?: memberId
        val idSpan = Span(
            if (idText != null) AppMessages.translateCurrent("member.number", idText)
            else AppMessages.translateCurrent("member.notAvailable")
        ).apply {
            addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY)
        }

        val homeCourseSpan = Span(
            matchedMember?.homeCourse ?: AppMessages.translateCurrent("member.notAvailable")
        ).apply {
            addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY)
        }

        val infoLayout = VerticalLayout(nameSpan, idSpan, homeCourseSpan).apply {
            isPadding = false
            isSpacing = false
        }

        val headerRow = HorizontalLayout(avatar, infoLayout).apply {
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }

        val unmatchedNote = if (matchedMember == null) {
            Span(AppMessages.translateCurrent("main.member.unmatched")).apply {
                addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.ERROR)
            }
        } else {
            null
        }

        return VerticalLayout().apply {
            isPadding = true
            isSpacing = true
            width = "280px"
            style["flex-shrink"] = "0"
            style["background"] = "var(--lumo-base-color)"
            style["border"] = "1px solid var(--lumo-contrast-10pct)"
            style["border-radius"] = "var(--lumo-border-radius-m)"
            add(H4(AppMessages.translateCurrent("main.member.profile")).apply {
                addClassNames(LumoUtility.Margin.NONE)
            })
            add(headerRow)
            if (unmatchedNote != null) add(unmatchedNote)
        }
    }

    private fun buildVerificationSummaryCard(result: FileVerificationResult): VerticalLayout {
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

        return VerticalLayout().apply {
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
    }

    private fun buildRoundsComparisonSection(comparisons: List<RoundComparison>): VerticalLayout {
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

        return VerticalLayout(title, grid).apply {
            setWidthFull()
            isPadding = true
            isSpacing = true
            style["background"] = "var(--lumo-base-color)"
            style["border"] = "1px solid var(--lumo-contrast-10pct)"
            style["border-radius"] = "var(--lumo-border-radius-m)"
            style["margin-top"] = "var(--lumo-space-m)"
        }
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
            isSpacing = false
            defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE
        }
    }

    private fun buildInitials(displayName: String?): String {
        if (displayName.isNullOrBlank()) return "?"
        val parts = displayName.trim().split(Regex("[\\s,]+")).filter { it.isNotBlank() }
        val first = parts.firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val second = parts.drop(1).firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return first + second
    }

    private fun statusText(status: VerificationStatus): String {
        return when (status) {
            VerificationStatus.PASS -> AppMessages.translateCurrent("main.status.complete.pass")
            VerificationStatus.WARNING -> AppMessages.translateCurrent("main.status.complete.warning")
            VerificationStatus.ALERT -> AppMessages.translateCurrent("main.status.complete.alert")
        }
    }

    private fun publishStatus(message: String) {
        eventPublisher.publishEvent(StatusUpdateEvent(message))
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}

