package com.kenjdavidson.golf.handicap.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Main application layout and dashboard.
 */
@Route("")
@PageTitle("Handicap Committee App")
@PermitAll
public class MainView extends AppLayout {

    private final AuthenticationContext authenticationContext;
    private final UserProfile userProfile;

    public MainView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        this.userProfile = resolveUserProfile();

        addToNavbar(buildNavbar());
        setContent(buildMainContent());
    }

    // -----------------------------------------------------------------------
    // Navbar / Header
    // -----------------------------------------------------------------------

    private Component buildNavbar() {
        H2 title = new H2("Golf Club Handicap Committee");
        title.addClassNames(
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.Margin.NONE
        );

        Span icon = new Span("⛳");
        icon.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.Right.SMALL
        );

        HorizontalLayout brand = new HorizontalLayout(icon, title);
        brand.setSpacing(false);
        brand.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(brand, buildUserSection());
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(brand);
        header.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);

        return header;
    }

    private Component buildUserSection() {
        MenuBar menuBar = new MenuBar();
        MenuItem menuItem = menuBar.addItem(VaadinIcon.CHEVRON_DOWN_SMALL.create());
        menuItem.getSubMenu().addItem("Log out", event -> authenticationContext.logout());

        Avatar avatar = new Avatar(userProfile.displayName());
        avatar.setAbbreviation(userProfile.initials());

        Span name = new Span(userProfile.displayName());
        name.addClassNames(LumoUtility.FontWeight.MEDIUM);

        Span details = new Span(userProfile.details());
        details.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        VerticalLayout info = new VerticalLayout(name, details);
        info.setPadding(false);
        info.setSpacing(false);
        info.setMargin(false);

        HorizontalLayout userSection = new HorizontalLayout(menuBar, avatar, info);
        userSection.setSpacing(true);
        userSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        return userSection;
    }

    private Component buildMainContent() {
        Component content = buildDashboardContent();
        HorizontalLayout shell = new HorizontalLayout();
        VerticalLayout wrapper = new VerticalLayout(buildPathToolbar(), content, buildStatusBar());
        wrapper.setSizeFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.expand(content);

        shell.add(wrapper);
        shell.setSizeFull();
        return shell;
    }

    private Component buildDashboardContent() {
        H2 heading = new H2("Welcome to the Handicap Committee App");
        heading.addClassName(LumoUtility.Margin.Bottom.SMALL);

        Span description = new Span(
            "Use the committee dashboard to access the current workspace and review " +
            "the application modules that will be expanded in future updates."
        );
        description.addClassName(LumoUtility.TextColor.SECONDARY);

        Div card = new Div(buildWelcomeCard("📄 PDF Parser",
                "Import and parse member round PDFs exported from Golf Canada."),
            buildWelcomeCard("🔍 Audit Log",
                "Review all changes made during this session."),
            buildWelcomeCard("👤 Member Rounds",
                "Browse individual member round history for the current season."),
            buildWelcomeCard("⚙️ Settings",
                "Configure Golf Canada API connection and display preferences.")
        );
        card.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
            .set("gap", "var(--lumo-space-m)")
            .set("margin-top", "var(--lumo-space-l)");

        VerticalLayout container = new VerticalLayout(heading, description, card);
        container.addClassNames(
            LumoUtility.Padding.LARGE,
            LumoUtility.MaxWidth.SCREEN_LARGE
        );
        container.setPadding(true);
        container.setSpacing(true);

        return container;
    }

    private Component buildPathToolbar() {
        H4 label = new H4("Workspace Folder");
        label.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.FontSize.SMALL
        );

        TextField filePath = new TextField();
        filePath.setReadOnly(true);
        filePath.setWidthFull();
        filePath.setValue("No folder selected");
        filePath.setPrefixComponent(VaadinIcon.FOLDER_OPEN.create());

        Button selectFolder = new Button("Select folder", VaadinIcon.FOLDER_OPEN_O.create());

        HorizontalLayout controls = new HorizontalLayout(filePath, selectFolder);
        controls.setWidthFull();
        controls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        controls.expand(filePath);

        VerticalLayout pathToolbar = new VerticalLayout(label, controls);
        pathToolbar.setWidthFull();
        pathToolbar.setSpacing(false);
        pathToolbar.setPadding(true);
        pathToolbar.getStyle()
            .set("margin", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)");

        return pathToolbar;
    }

    private Component buildStatusBar() {
        Span leftStatus = new Span("Status: Ready");
        leftStatus.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        Span rightStatus = new Span("Authentication active");
        rightStatus.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        HorizontalLayout statusBar = new HorizontalLayout(leftStatus, rightStatus);
        statusBar.setWidthFull();
        statusBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        statusBar.expand(leftStatus);
        statusBar.addClassNames(
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.SMALL
        );
        statusBar.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        return statusBar;
    }

    /**
     * Creates a simple module-overview card for the dashboard placeholder.
     */
    private Div buildWelcomeCard(String title, String subtitle) {
        Span cardTitle = new Span(title);
        cardTitle.addClassNames(
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.FontWeight.SEMIBOLD
        );

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        Div card = new Div(cardTitle, cardSubtitle);
        card.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "var(--lumo-space-xs)")
            .set("padding", "var(--lumo-space-m)")
            .set("background", "var(--lumo-base-color)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)");

        return card;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private UserProfile resolveUserProfile() {
        Optional<UserDetails> authenticatedUser = authenticationContext.getAuthenticatedUser(UserDetails.class);

        return authenticatedUser
            .map(user -> new UserProfile(
                user.getUsername(),
                user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("Authenticated user"),
                buildInitials(user.getUsername())
            ))
            .orElse(new UserProfile("Committee User", "Authenticated user", "CU"));
    }

    private String buildInitials(String displayName) {
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "CU";
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1).toUpperCase() : "";
        return first + second;
    }

    private record UserProfile(String displayName, String details, String initials) {
    }
}
