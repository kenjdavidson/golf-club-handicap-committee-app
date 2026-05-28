package com.kenjdavidson.golf.handicap.components;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoggingMessageServiceTest {

    @Test
    void logErrorStoresMessageAndPublishesEvent() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LoggingMessageService service = new LoggingMessageService(publisher);

        LogMessage logged = service.logError("Something went wrong", null);

        assertNotNull(logged);
        assertEquals("Something went wrong", logged.getMessage());
        assertNotNull(logged.getTimestamp());
        assertTrue(service.hasMessages());
        assertEquals(1, service.getMessageCount());
        verify(publisher, times(1)).publishEvent(any(ErrorLoggedEvent.class));
    }

    @Test
    void logErrorWithCauseStoresStackTrace() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LoggingMessageService service = new LoggingMessageService(publisher);
        RuntimeException cause = new RuntimeException("Root cause");

        LogMessage logged = service.logError("Error occurred", cause);

        assertNotNull(logged.getStackTrace());
        assertTrue(logged.getStackTrace().contains("RuntimeException"));
    }

    @Test
    void getMessagesReturnsInReverseChronologicalOrder() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LoggingMessageService service = new LoggingMessageService(publisher);

        service.logError("First error", null);
        service.logError("Second error", null);

        List<LogMessage> messages = service.getMessages();
        assertEquals(2, messages.size());
        // getMessages() returns in reverse insertion order (most-recent first)
        assertEquals("Second error", messages.get(0).getMessage());
        assertEquals("First error", messages.get(1).getMessage());
    }

    @Test
    void clearMessagesRemovesAllEntries() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LoggingMessageService service = new LoggingMessageService(publisher);
        service.logError("Error one", null);
        service.logError("Error two", null);

        service.clearMessages();

        assertFalse(service.hasMessages());
        assertEquals(0, service.getMessageCount());
        assertTrue(service.getMessages().isEmpty());
    }
}
