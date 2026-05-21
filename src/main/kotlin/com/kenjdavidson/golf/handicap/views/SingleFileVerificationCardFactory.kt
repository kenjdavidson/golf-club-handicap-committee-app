package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class SingleFileVerificationCardFactory(
    private val singleFileVerificationService: SingleFileVerificationService,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun create(authenticatedUser: GolfCanadaAuthenticatedUser): Div {
        val uploadBuffer = MemoryBuffer()
        val uploadButton = Button("Upload file", VaadinIcon.UPLOAD.create())
        val upload = Upload(uploadBuffer).apply {
            setAcceptedFileTypes(".pdf")
            isAutoUpload = true
            setUploadButton(uploadButton)
            setDropAllowed(false)
            style["padding"] = "0"
            style["border"] = "none"
            style["min-height"] = "0"
        }
        val verificationResult = Div()
        var uploadedBytes: ByteArray? = null
        var uploadedFileName: String? = null

        val verifyButton = Button("Verify", VaadinIcon.CHECK.create()).apply {
            isEnabled = false
            addClickListener {
                val fileBytes = uploadedBytes ?: return@addClickListener
                val fileName = uploadedFileName ?: "uploaded.pdf"

                try {
                    publishStatus("Verifying $fileName...")
                    val result = singleFileVerificationService.verify(fileName, fileBytes, authenticatedUser)
                    renderResult(result, verificationResult)
                    publishStatus(statusText(result.status))
                } catch (exception: VerificationProcessingException) {
                    val message = exception.message ?: "Verification failed."
                    publishStatus(message)
                    verificationResult.removeAll()
                }
            }
        }

        upload.addSucceededListener { event ->
            uploadedBytes = uploadBuffer.inputStream.readBytes()
            uploadedFileName = event.fileName
            verifyButton.isEnabled = uploadedBytes?.isNotEmpty() == true
            publishStatus("Uploaded ${event.fileName}")
        }

        upload.addFileRejectedListener { event ->
            uploadedBytes = null
            uploadedFileName = null
            verifyButton.isEnabled = false
            publishStatus(event.errorMessage)
            verificationResult.removeAll()
        }

        val controls = HorizontalLayout(upload, verifyButton).apply {
            setWidthFull()
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        }

        return Div(
            controls,
            verificationResult
        ).apply {
            style["display"] = "flex"
            style["flex-direction"] = "column"
            style["gap"] = "var(--lumo-space-s)"
            style["padding"] = "var(--lumo-space-m)"
            style["background"] = "var(--lumo-contrast-5pct)"
            style["border"] = "1px solid var(--lumo-contrast-10pct)"
            style["border-radius"] = "var(--lumo-border-radius-l)"
        }
    }

    private fun renderResult(result: FileVerificationResult, verificationResult: Div) {
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
