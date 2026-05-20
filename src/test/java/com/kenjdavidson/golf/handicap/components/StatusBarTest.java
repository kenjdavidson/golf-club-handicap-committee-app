package com.kenjdavidson.golf.handicap.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusBarTest {

    @Test
    void updateStatusReplacesDisplayedText() {
        StatusBar statusBar = new StatusBar();

        statusBar.updateStatus("Status: Processing");

        assertTrue(containsText(statusBar, "Status: Processing"));
    }

    @Test
    void eventListenerUpdatesDisplayedText() {
        StatusBar statusBar = new StatusBar();

        statusBar.onStatusUpdate(new StatusUpdateEvent("Status: Verification complete"));

        assertTrue(containsText(statusBar, "Status: Verification complete"));
    }

    private boolean containsText(Component component, String expected) {
        return matchesComponent(component, candidate ->
            candidate instanceof HasText hasText && expected.equals(hasText.getText())
                || candidate instanceof Button button && expected.equals(button.getText())
                || candidate instanceof Span span && expected.equals(span.getText())
        );
    }

    private boolean matchesComponent(Component component, Predicate<Component> predicate) {
        return predicate.test(component) || component.getChildren().anyMatch(child -> matchesComponent(child, predicate));
    }
}
