package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.NonUniqueMemberFoundException
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import com.kenjdavidson.golf.handicap.verification.VerificationStatus
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll
import org.springframework.context.ApplicationEventPublisher

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
        uploadCard.style["position"] = "sticky"
        uploadCard.style["top"] = "0"
        uploadCard.style["z-index"] = "1"
        uploadCard.style["background"] = "var(--lumo-base-color)"
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

        setWidthFull()
        isPadding = true
        isSpacing = true
        element.setAttribute("tabindex", "0")
        element.setAttribute("aria-label", AppMessages.translateCurrent("main.aria.mainContent"))
        add(uploadCard, verificationResult)
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
        } catch (exception: NonUniqueMemberFoundException) {
            val message = AppMessages.translateCurrent("main.status.failed")
            publishStatus(message)
            renderCandidates(exception)
        } catch (exception: VerificationProcessingException) {
            val message = exception.message ?: AppMessages.translateCurrent("main.status.failed")
            publishStatus(message)
            renderError(message)
        }
    }

    private fun renderResult(result: FileVerificationResult) {
        verificationResult.removeAll()

        val topRow = HorizontalLayout().apply {
            setWidthFull()
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.STRETCH
        }

        val profileCard = MemberProfileCard(result.memberProfile)
        val summaryCard = VerificationSummaryCard(result)

        topRow.add(profileCard, summaryCard)
        topRow.setFlexGrow(1.0, profileCard, summaryCard)

        verificationResult.add(topRow)

        if (result.roundComparisons.isNotEmpty()) {
            verificationResult.add(RoundsComparisonGrid(result.roundComparisons))
        }
    }

    private fun renderError(message: String) {
        verificationResult.removeAll()
        verificationResult.add(Paragraph(message))
    }

    private fun renderCandidates(exception: NonUniqueMemberFoundException) {
        verificationResult.removeAll()
        verificationResult.add(Paragraph(exception.message))
        val list = UnorderedList()
        exception.candidates.forEach { candidate ->
            val label = listOfNotNull(candidate.name, candidate.club, candidate.region)
                .joinToString(" — ")
            list.add(ListItem(label))
        }
        verificationResult.add(list)
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
}
