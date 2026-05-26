package com.kenjdavidson.golf.handicap.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LoginViewTest {

    @Test
    void doesNotRenderStatusBarContent() {
        LoginView view = new LoginView();

        assertFalse(containsText(view, "Status: Ready"));
        assertFalse(containsText(view, "Logged in as"));
    }

    private boolean containsText(Component component, String expected) {
        return matchesComponent(component, candidate ->
            candidate instanceof HasText hasText && hasText.getText().contains(expected)
        );
    }

    private boolean matchesComponent(Component component, Predicate<Component> predicate) {
        return predicate.test(component) || component.getChildren().anyMatch(child -> matchesComponent(child, predicate));
    }
}
