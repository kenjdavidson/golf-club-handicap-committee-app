package com.kenjdavidson.golf.handicap.components;

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatusBarTest {

    @Test
    void updateStatusReplacesDisplayedText() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        UserProfileResolver userProfileResolver = mock(UserProfileResolver.class);
        GolfCanadaAuthenticatedUser user = authenticatedUser();
        when(authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser.class)).thenReturn(Optional.of(user));
        when(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(user);
        when(userProfileResolver.buildUserProfile(user)).thenReturn(
            new UserProfile("Committee User", "committee.user@example.com • HCP 8.4 • Gold", "CU")
        );
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver);
        UI ui = attachToUi(statusBar);

        try {
            assertDoesNotThrow(() -> statusBar.updateStatus("Status: Processing"));
            ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

            assertTrue(statusBar.getElement().getTextRecursively().contains("Status: Processing"));
            assertTrue(containsText(statusBar, "Logged in as Committee User"));
        } finally {
            UI.setCurrent(null);
        }
    }

    @Test
    void statusUpdateEventReplacesDisplayedText() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        UserProfileResolver userProfileResolver = mock(UserProfileResolver.class);
        GolfCanadaAuthenticatedUser user = authenticatedUser();
        when(authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser.class)).thenReturn(Optional.of(user));
        when(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(user);
        when(userProfileResolver.buildUserProfile(user)).thenReturn(
            new UserProfile("Committee User", "committee.user@example.com • HCP 8.4 • Gold", "CU")
        );
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver);
        UI ui = attachToUi(statusBar);

        try {
            assertDoesNotThrow(() -> statusBar.onStatusUpdate(new StatusUpdateEvent("Status: Event Processing")));
            ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

            assertTrue(statusBar.getElement().getTextRecursively().contains("Status: Event Processing"));
        } finally {
            UI.setCurrent(null);
        }
    }

    private boolean containsText(Component component, String expected) {
        return matchesComponent(component, candidate ->
            candidate instanceof HasText hasText && expected.equals(hasText.getText())
        );
    }

    private boolean matchesComponent(Component component, Predicate<Component> predicate) {
        return predicate.test(component) || component.getChildren().anyMatch(child -> matchesComponent(child, predicate));
    }

    private UI attachToUi(Component component) {
        UI ui = new UI();
        UI.setCurrent(ui);
        ui.add(component);
        return ui;
    }

    private GolfCanadaAuthenticatedUser authenticatedUser() {
        return new GolfCanadaAuthenticatedUser(
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
    }
}
