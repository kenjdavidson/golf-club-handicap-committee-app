package com.kenjdavidson.golf.handicap.components

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

@SessionScope
@Component
class LoggingMessageService(
    private val eventPublisher: ApplicationEventPublisher
) {
    private val messages = CopyOnWriteArrayList<LogMessage>()

    fun logError(message: String, cause: Throwable? = null): LogMessage {
        val logMessage = LogMessage(
            timestamp = Instant.now(),
            message = message,
            stackTrace = cause?.let { formatStackTrace(it) }
        )
        messages.add(logMessage)
        eventPublisher.publishEvent(ErrorLoggedEvent(message))
        return logMessage
    }

    fun getMessages(): List<LogMessage> = messages.reversed()

    fun getMessageCount(): Int = messages.size

    fun hasMessages(): Boolean = messages.isNotEmpty()

    fun clearMessages() {
        messages.clear()
    }

    private fun formatStackTrace(cause: Throwable): String {
        val sw = StringWriter()
        cause.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
