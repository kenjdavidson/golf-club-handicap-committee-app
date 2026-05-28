package com.kenjdavidson.golf.handicap.views;

import com.kenjdavidson.golf.handicap.components.LoggingMessageService;
import com.kenjdavidson.golf.handicap.components.Navbar;
import com.kenjdavidson.golf.handicap.components.StatusBar;
import com.kenjdavidson.golf.handicap.components.UserProfile;
import com.kenjdavidson.golf.handicap.components.UserProfileResolver;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedViewTest {

    @Test
    void contentLayoutDoesNotForceFullscreenScrollingContainer() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        UserProfileResolver userProfileResolver = mock(UserProfileResolver.class);
        LoggingMessageService loggingMessageService = mock(LoggingMessageService.class);
        GolfCanadaAuthenticatedUser user = authenticatedUser();

        when(authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser.class)).thenReturn(Optional.of(user));
        when(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(user);
        when(userProfileResolver.buildUserProfile(user)).thenReturn(
            new UserProfile("Committee User", "committee.user@example.com • HCP 8.4 • Gold", "CU")
        );
        when(loggingMessageService.getMessageCount()).thenReturn(0);

        Navbar navbar = new Navbar(authenticationContext, userProfileResolver);
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver, loggingMessageService);
        AuthenticatedView view = new AuthenticatedView(navbar, statusBar);

        HorizontalLayout mainMenuPanel = readMainMenuPanel(shell);
        assertFalse(mainMenuPanel.isVisible());
        assertEquals("var(--lumo-space-m)", mainMenuPanel.getStyle().get("padding-left"));
        assertEquals("var(--lumo-space-m)", mainMenuPanel.getStyle().get("padding-right"));

        VerticalLayout navbarPanel = readNavbarPanel(shell);
        List<Component> toolbarRows = navbarPanel.getChildren().toList();
        assertEquals(2, toolbarRows.size());
        assertEquals(navbar, toolbarRows.get(0));
        assertEquals(mainMenuPanel, toolbarRows.get(1));

        VerticalLayout content = assertInstanceOf(VerticalLayout.class, shell.getContent());
        List<Component> rows = content.getChildren().toList();
        assertEquals(2, rows.size());
        assertInstanceOf(Div.class, rows.get(0));
        assertEquals(statusBar, rows.get(1));

        readAppMenuButton(navbar).click();
        assertTrue(mainMenuPanel.isVisible());
        assertEquals("pointer", readSingleFileButton(shell).getStyle().get("cursor"));
        assertEquals("pointer", readWorkspaceButton(shell).getStyle().get("cursor"));
    }

    private HorizontalLayout readMainMenuPanel(AuthenticatedView shell) throws Exception {
        VerticalLayout content = assertInstanceOf(VerticalLayout.class, view.getContent());
        assertEquals("100%", content.getWidth());
        assertTrue(isMissing(content.getHeight()));

        List<Component> contentChildren = content.getChildren().toList();
        Div viewContainer = assertInstanceOf(Div.class, contentChildren.getFirst());
        assertEquals("100%", viewContainer.getWidth());
        assertTrue(isMissing(viewContainer.getHeight()));
        assertEquals(0.0, content.getFlexGrow(viewContainer));
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private Button readSingleFileButton(AuthenticatedView shell) throws Exception {
        var field = AuthenticatedView.class.getDeclaredField("singleFileButton");
        field.setAccessible(true);
        return (Button) field.get(shell);
    }

    private Button readWorkspaceButton(AuthenticatedView shell) throws Exception {
        var field = AuthenticatedView.class.getDeclaredField("workspaceButton");
        field.setAccessible(true);
        return (Button) field.get(shell);
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
