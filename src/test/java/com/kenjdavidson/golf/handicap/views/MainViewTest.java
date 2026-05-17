package com.kenjdavidson.golf.handicap.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainViewTest {

    @Test
    void rendersAuthenticatedUserAndWorkspaceControls() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        UserDetails user = User.withUsername("committee.user")
            .password("ignored")
            .roles("COMMITTEE")
            .build();

        when(authenticationContext.getAuthenticatedUser(UserDetails.class)).thenReturn(Optional.of(user));

        MainView view = new MainView(authenticationContext);

        assertTrue(containsText(view, "committee.user"));
        assertTrue(containsText(view, "Select folder"));
        assertTrue(containsText(view, "Status: Ready"));
        assertTrue(containsTextFieldValue(view, "No folder selected"));
    }

    private boolean containsText(Component component, String expected) {
        if (component instanceof HasText hasText && expected.equals(hasText.getText())) {
            return true;
        }

        if (component instanceof Button button && expected.equals(button.getText())) {
            return true;
        }

        if (component instanceof Span span && expected.equals(span.getText())) {
            return true;
        }

        return component.getChildren().anyMatch(child -> containsText(child, expected));
    }

    private boolean containsTextFieldValue(Component component, String expected) {
        if (component instanceof TextField textField && expected.equals(textField.getValue())) {
            return true;
        }

        return component.getChildren().anyMatch(child -> containsTextFieldValue(child, expected));
    }
}
