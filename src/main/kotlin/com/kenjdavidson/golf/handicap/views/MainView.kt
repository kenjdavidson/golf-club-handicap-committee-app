package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
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
        val mismatches = if (result.mismatchedDates.isEmpty()) {
            AppMessages.translateCurrent("main.result.none")
        } else {
            result.mismatchedDates.joinToString(", ") { it.format(DATE_FORMATTER) }
        }
        val notes = if (result.notes.isEmpty()) {
            ""
        } else {
            "<br/><strong>${AppMessages.translateCurrent("main.result.notes")}:</strong> ${result.notes.joinToString("; ")}"
        }

        verificationResult.removeAll()
        verificationResult.add(
            Html(
                "<div>" +
                    "<strong>${AppMessages.translateCurrent("main.result.status")}:</strong> ${result.status}<br/>" +
                    "<strong>${AppMessages.translateCurrent("main.result.dateMatch")}:</strong> ${result.matchPercentage}% " +
                    "(${result.matchedCount}/${result.comparedCount})<br/>" +
                    "<strong>${AppMessages.translateCurrent("main.result.mismatchedDates")}:</strong> $mismatches" +
                    notes +
                    "</div>"
            )
        )
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
        val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
