package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
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
) : VerticalLayout() {

    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val verificationResult = Div()

    init {
        val uploadCard = SingleFileUploadCard()
        uploadCard.setFileSelectedListener { fileName ->
            publishStatus("Uploaded $fileName")
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
        element.setAttribute("aria-label", "Main content")
        add(
            Div(uploadCard, verificationResult).apply {
                style["display"] = "flex"
                style["flex-direction"] = "column"
                style["gap"] = "var(--lumo-space-s)"
                style["padding"] = "var(--lumo-space-m)"
                style["background"] = "var(--lumo-contrast-5pct)"
                style["border"] = "1px solid var(--lumo-contrast-10pct)"
                style["border-radius"] = "var(--lumo-border-radius-l)"
            }
        )
    }

    private fun verifyFile(fileName: String, fileBytes: ByteArray) {
        try {
            publishStatus("Verifying $fileName...")
            val result = singleFileVerificationService.verify(fileName, fileBytes, authenticatedUser)
            renderResult(result)
            publishStatus(statusText(result.status))
        } catch (exception: VerificationProcessingException) {
            val message = exception.message ?: "Verification failed."
            publishStatus(message)
            verificationResult.removeAll()
        }
    }

    private fun renderResult(result: FileVerificationResult) {
        val mismatches = if (result.mismatchedDates.isEmpty()) {
            "None"
        } else {
            result.mismatchedDates.joinToString(", ") { it.format(DATE_FORMATTER) }
        }
        val notes = if (result.notes.isEmpty()) {
            ""
        } else {
            "<br/><strong>Notes:</strong> ${result.notes.joinToString("; ")}"
        }

        verificationResult.removeAll()
        verificationResult.add(
            Html(
                "<div>" +
                    "<strong>Status:</strong> ${result.status}<br/>" +
                    "<strong>Date Match:</strong> ${result.matchPercentage}% " +
                    "(${result.matchedCount}/${result.comparedCount})<br/>" +
                    "<strong>Mismatched Dates:</strong> $mismatches" +
                    notes +
                    "</div>"
            )
        )
    }

    private fun statusText(status: VerificationStatus): String {
        return when (status) {
            VerificationStatus.PASS -> "Verification complete: pass."
            VerificationStatus.WARNING -> "Verification complete: warning."
            VerificationStatus.ALERT -> "Verification complete: alert."
        }
    }

    private fun publishStatus(message: String) {
        eventPublisher.publishEvent(StatusUpdateEvent(message))
    }

    private companion object {
        val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
