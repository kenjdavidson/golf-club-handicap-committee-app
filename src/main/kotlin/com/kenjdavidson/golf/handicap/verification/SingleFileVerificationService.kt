package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.components.StatusUpdateEvent
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.verification.steps.SingleFileVerificationStep
import com.kenjdavidson.golf.handicap.util.operation
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class SingleFileVerificationService(
    private val verificationSteps: List<SingleFileVerificationStep>,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun verify(
        fileName: String,
        fileBytes: ByteArray,
        authenticatedUser: GolfCanadaAuthenticatedUser
    ): FileVerificationResult = operation("Verifying file: $fileName") {
        if (fileBytes.isEmpty()) {
            throw VerificationProcessingException("Uploaded file is empty.")
        }

        val context = verificationSteps.fold(
            VerificationContext(
                fileName = fileName,
                fileBytes = fileBytes,
                authenticatedUser = authenticatedUser
            )
        ) { current, step ->
            eventPublisher.publishEvent(StatusUpdateEvent(step.statusMessage()))
            step.process(current)
        }

        context.result
            ?: throw VerificationProcessingException("Verification pipeline completed without a verification result.")
    }
}
