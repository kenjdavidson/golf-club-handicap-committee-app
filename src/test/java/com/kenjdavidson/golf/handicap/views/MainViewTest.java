package com.kenjdavidson.golf.handicap.views;

import com.kenjdavidson.golf.handicap.components.Navbar;
import com.kenjdavidson.golf.handicap.components.StatusBar;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainViewTest {

    @Test
    void rendersAuthenticatedUserAndNavigationShell() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        GolfCanadaAuthenticatedUser user = new GolfCanadaAuthenticatedUser(
            new AuthToken().accessToken("access-token").user(new User()
                .username("committee.user")
                .fullName("Committee User")
                .email("committee.user@example.com")
                .golfCanadaCardId("1234567")
                .handicap("8.4")
                .membershipLevel("Gold")),
            "committee.user",
            "Committee User",
            "committee.user@example.com",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser.class)).thenReturn(Optional.of(user));
        UserProfileResolver userProfileResolver = mock(UserProfileResolver.class);
        when(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(user);
        when(userProfileResolver.buildUserProfile(user)).thenReturn(
            new UserProfile("Committee User", "committee.user@example.com • HCP 8.4 • Gold", "CU")
        );
        SingleFileVerificationService verificationService = mock(SingleFileVerificationService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        Navbar navbar = new Navbar(authenticationContext, userProfileResolver);
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver);

        MainView view = new MainView(authenticationContext, userProfileResolver, verificationService, eventPublisher);
        AuthenticatedView shell = new AuthenticatedView(navbar, statusBar);
        shell.showRouterLayoutContent(view);

        assertTrue(containsText(shell, "Committee User"));
        assertTrue(containsText(shell, "Member #1234567"));
        assertTrue(containsText(shell, "Verify"));
        assertTrue(containsText(shell, "Status: Ready"));
        assertTrue(containsText(shell, "Logged in as Committee User"));
        assertTrue(containsText(shell, "Lookup"));
        assertTrue(containsText(shell, "Settings"));
        assertFalse(containsText(shell, "Golf Club Handicap Committee"));
        assertFalse(containsText(shell, "committee.user@example.com • HCP 8.4 • Gold"));
        assertFalse(containsText(shell, "Workspace Folder"));
        assertFalse(containsText(shell, "Welcome to the Handicap Committee App"));
        assertFalse(containsTextFieldValue(shell, "No folder selected"));
    }

    private boolean containsText(Component component, String expected) {
        return matchesComponent(component, candidate ->
            candidate instanceof HasText hasText && expected.equals(hasText.getText())
                || candidate instanceof Button button && expected.equals(button.getText())
                || candidate instanceof Span span && expected.equals(span.getText())
        );
    }

    private boolean containsTextFieldValue(Component component, String expected) {
        return matchesComponent(component, candidate ->
            candidate instanceof TextField textField && expected.equals(textField.getValue())
        );
    }

    private boolean matchesComponent(Component component, Predicate<Component> predicate) {
        return predicate.test(component) || component.getChildren().anyMatch(child -> matchesComponent(child, predicate));
    }
}
