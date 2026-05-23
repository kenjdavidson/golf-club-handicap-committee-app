package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.GolfCanadaMemberMatch
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.theme.lumo.LumoUtility

class MemberProfileCard(
    matchedMember: GolfCanadaMemberMatch?,
    playerName: String?,
    memberId: String?
) : VerticalLayout() {

    init {
        val displayName = matchedMember?.fullName ?: playerName ?: AppMessages.translateCurrent("member.notAvailable")
        val avatar = Avatar(displayName).apply {
            abbreviation = buildInitials(displayName)
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

        if (matchedMember == null) {
            add(Span(AppMessages.translateCurrent("main.member.unmatched")).apply {
                addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.ERROR)
            })
        }
    }

    private fun buildInitials(displayName: String?): String {
        if (displayName.isNullOrBlank()) return "?"
        val parts = displayName.trim().split(Regex("[\\s,]+")).filter { it.isNotBlank() }
        val first = parts.firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val second = parts.drop(1).firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return first + second
    }
}
