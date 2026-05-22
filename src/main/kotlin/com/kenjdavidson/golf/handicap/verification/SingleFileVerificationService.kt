package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.util.operation
import org.springframework.stereotype.Service

@Service
class SingleFileVerificationService(
    private val verificationSteps: List<SingleFileVerificationStep>
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
        ) { current, step -> step.process(current) }

        context.result
            ?: throw VerificationProcessingException("Verification pipeline completed without a verification result.")
    }
}
