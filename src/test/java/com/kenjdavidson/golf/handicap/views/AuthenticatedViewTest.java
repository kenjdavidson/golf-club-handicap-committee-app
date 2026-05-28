package com.kenjdavidson.golf.handicap.views;

import com.kenjdavidson.golf.handicap.components.Navbar;
import com.kenjdavidson.golf.handicap.components.LoggingMessageService;
import com.kenjdavidson.golf.handicap.components.StatusBar;
import com.kenjdavidson.golf.handicap.components.UserProfile;
import com.kenjdavidson.golf.handicap.components.UserProfileResolver;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedViewTest {
    @Test
    void rendersStatusBarAsBottomRowAndMainMenuAsSecondToolbar() throws Exception {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        UserProfileResolver userProfileResolver = mock(UserProfileResolver.class);
        GolfCanadaAuthenticatedUser user = authenticatedUser();
        when(authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser.class)).thenReturn(Optional.of(user));
        when(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(user);
        when(userProfileResolver.buildUserProfile(user)).thenReturn(
            new UserProfile("Committee User", "committee.user@example.com • HCP 8.4 • Gold", "CU")
        );

        LoggingMessageService loggingMessageService = mock(LoggingMessageService.class);
        Navbar navbar = new Navbar(authenticationContext, userProfileResolver);
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver, loggingMessageService);
        AuthenticatedView shell = new AuthenticatedView(navbar, statusBar);

        HorizontalLayout mainMenuPanel = readMainMenuPanel(shell);
        assertFalse(mainMenuPanel.isVisible());

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
    }

    private HorizontalLayout readMainMenuPanel(AuthenticatedView shell) throws Exception {
        var field = AuthenticatedView.class.getDeclaredField("mainMenuPanel");
        field.setAccessible(true);
        return (HorizontalLayout) field.get(shell);
    }

    private VerticalLayout readNavbarPanel(AuthenticatedView shell) throws Exception {
        var field = AuthenticatedView.class.getDeclaredField("navbarPanel");
        field.setAccessible(true);
        return (VerticalLayout) field.get(shell);
    }

    private Button readAppMenuButton(Navbar navbar) throws Exception {
        var field = Navbar.class.getDeclaredField("appMenuButton");
        field.setAccessible(true);
        return (Button) field.get(navbar);
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
