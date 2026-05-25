package com.kenjdavidson.golf.handicap.views;

import com.kenjdavidson.golf.handicap.components.Navbar;
import com.kenjdavidson.golf.handicap.components.StatusBar;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.kenjdavidson.golf.handicap.verification.SingleFileVerificationService;
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class MainViewTest {

    @Test
    void rendersAuthenticatedUserAndTopLevelShell() {
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
        Navbar navbar = new Navbar("Golf Handicap App", authenticationContext, userProfileResolver);
        StatusBar statusBar = new StatusBar(authenticationContext, userProfileResolver);

        // Vaadin 25 requires an active service/UI context for several components:
        //   - UploadManager needs UI.getCurrentOrThrow() to register a stream resource.
        //   - RouterLink needs VaadinService.getCurrent().getRouter() to resolve routes.
        //   - RouteConfiguration.forRegistry() needs the route registry to be populated.
        // None of these are relevant to what this test is asserting (nav/user content in the
        // shell), so we mock them at the lowest level needed to let construction complete.
        //
        // RouteConfiguration: return empty string for any getUrl variant (avoids compile-time
        // overload ambiguity between getUrl(Class,RouteParameters) and getUrl(Class,T)).
        RouteConfiguration mockRouteConfig = mock(RouteConfiguration.class,
                inv -> "getUrl".equals(inv.getMethod().getName()) ? "" : RETURNS_DEFAULTS.answer(inv));

        try (MockedConstruction<UploadManager> ignored = mockConstruction(UploadManager.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
             MockedStatic<VaadinService> ignored2 = mockStatic(VaadinService.class, RETURNS_DEEP_STUBS);
             MockedStatic<RouteConfiguration> routeConfigStatic = mockStatic(RouteConfiguration.class)) {
            routeConfigStatic.when(() -> RouteConfiguration.forRegistry(any())).thenReturn(mockRouteConfig);
            MainView view = new MainView(authenticationContext, userProfileResolver, verificationService, eventPublisher);
            AuthenticatedView shell = new AuthenticatedView(navbar, statusBar);
            shell.showRouterLayoutContent(view);

            List<Component> viewChildren = view.getChildren().collect(Collectors.toList());
            assertTrue(viewChildren.size() == 2);
            assertTrue("sticky".equals(viewChildren.get(0).getElement().getStyle().get("position")));
            assertTrue(containsText(shell, "Verify"));
            assertTrue(containsText(shell, "Logged in as Committee User"));
            assertFalse(containsText(shell, "Lookup"));
            assertFalse(containsText(shell, "Settings"));
            assertFalse(containsText(shell, "Golf Club Handicap Committee"));
            assertFalse(containsText(shell, "committee.user@example.com • HCP 8.4 • Gold"));
            assertFalse(containsText(shell, "Workspace Folder"));
            assertFalse(containsText(shell, "Welcome to the Handicap Committee App"));
            assertFalse(containsTextFieldValue(shell, "No folder selected"));
        }
    }

    @Test
    void rendersVerificationErrorsInMainContent() throws Exception {
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
        when(verificationService.verify(anyString(), any(byte[].class), any(GolfCanadaAuthenticatedUser.class)))
            .thenThrow(new VerificationProcessingException("No valid played dates were found in the uploaded PDF.", null));
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        RouteConfiguration mockRouteConfig = mock(RouteConfiguration.class,
            inv -> "getUrl".equals(inv.getMethod().getName()) ? "" : RETURNS_DEFAULTS.answer(inv));

        try (MockedConstruction<UploadManager> ignored = mockConstruction(UploadManager.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
             MockedStatic<VaadinService> ignored2 = mockStatic(VaadinService.class, RETURNS_DEEP_STUBS);
             MockedStatic<RouteConfiguration> routeConfigStatic = mockStatic(RouteConfiguration.class)) {
            routeConfigStatic.when(() -> RouteConfiguration.forRegistry(any())).thenReturn(mockRouteConfig);
            MainView view = new MainView(authenticationContext, userProfileResolver, verificationService, eventPublisher);

            var verifyFile = MainView.class.getDeclaredMethod("verifyFile", String.class, byte[].class);
            verifyFile.setAccessible(true);
            verifyFile.invoke(view, "Adderley, Jim - May 12.pdf", "pdf".getBytes(StandardCharsets.UTF_8));

            assertTrue(containsText(view, "No valid played dates were found in the uploaded PDF."));
        }
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
