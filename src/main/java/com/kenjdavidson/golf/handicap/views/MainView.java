package com.kenjdavidson.golf.handicap.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Main application layout and dashboard.
 *
 * <p>Uses the Vaadin {@link AppLayout} component which provides the standard
 * Lumo-themed shell with a collapsible left drawer and a top navigation bar.
 * The layout consists of:
 * <ul>
 *   <li><strong>Drawer (sidebar)</strong> – modular navigation links.  New
 *       modules (PDF Parser, Audit, etc.) register their own routes and appear
 *       here automatically once added.</li>
 *   <li><strong>Header (navbar)</strong> – application title and a live
 *       status badge indicating the local-database connection state.</li>
 *   <li><strong>Content area</strong> – a placeholder {@code ModuleContainer}
 *       that child views populate via Vaadin's router.</li>
 * </ul>
 */
@Route("")
@PageTitle("Handicap Committee App")
public class MainView extends AppLayout {

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(buildDrawerContent());
        addToNavbar(buildNavbar());
    }

    // -----------------------------------------------------------------------
    // Drawer / Sidebar
    // -----------------------------------------------------------------------

    private Component buildDrawerContent() {
        // Application brand at the top of the drawer
        H1 appName = new H1("⛳ Handicap App");
        appName.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );

        SideNav nav = buildNavigation();

        VerticalLayout drawerLayout = new VerticalLayout(appName, nav);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.getStyle().set("background", "var(--lumo-base-color)");

        return drawerLayout;
    }

    /**
     * Builds the primary navigation menu.
     *
     * <p>Each {@link SideNavItem} represents a module.  Route targets are
     * defined as string paths here so new modules can be added by simply
     * registering a {@code @Route} view class.
     */
    private SideNav buildNavigation() {
        SideNav nav = new SideNav();
        nav.setWidthFull();

        SideNavItem dashboard = new SideNavItem("Dashboard", "/", VaadinIcon.HOME.create());

        SideNavItem pdfParser = new SideNavItem("PDF Parser", "/pdf-parser",
            VaadinIcon.FILE_TEXT.create());
        pdfParser.setSuffixComponent(buildBadge("NEW"));

        SideNavItem auditLog = new SideNavItem("Audit Log", "/audit",
            VaadinIcon.LIST.create());

        SideNavItem memberRounds = new SideNavItem("Member Rounds", "/member-rounds",
            VaadinIcon.USER_CARD.create());

        SideNavItem settings = new SideNavItem("Settings", "/settings",
            VaadinIcon.COG.create());

        nav.addItem(dashboard, pdfParser, auditLog, memberRounds, settings);
        return nav;
    }

    // -----------------------------------------------------------------------
    // Navbar / Header
    // -----------------------------------------------------------------------

    private Component buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();

        H2 title = new H2("Golf Club Handicap Committee");
        title.addClassNames(
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.Margin.NONE
        );

        HorizontalLayout header = new HorizontalLayout(toggle, title, buildStatusIndicator());
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(title);
        header.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);

        return header;
    }

    /**
     * Returns a small badge that shows the local-database connection status.
     *
     * <p>Because the application uses an in-memory H2 database that is always
     * initialised on startup, the status is always "Connected" while the
     * process is running.
     */
    private Component buildStatusIndicator() {
        Span dot = new Span("●");
        dot.getStyle()
            .set("color", "var(--lumo-success-color)")
            .set("font-size", "0.75rem")
            .set("margin-right", "4px");

        Span label = new Span("Connected to local database");
        label.addClassNames(
            LumoUtility.FontSize.XSMALL,
            LumoUtility.TextColor.SECONDARY
        );

        Span status = new Span(dot, label);
        status.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("white-space", "nowrap");

        return status;
    }

    // -----------------------------------------------------------------------
    // Default content (Module Container placeholder)
    // -----------------------------------------------------------------------

    /**
     * When no child route is active, display a welcoming placeholder that
     * explains the module system to new users.
     */
    private Component buildModuleContainerPlaceholder() {
        H2 heading = new H2("Welcome to the Handicap Committee App");
        heading.addClassName(LumoUtility.Margin.Bottom.SMALL);

        Span description = new Span(
            "Select a module from the sidebar to get started. " +
            "Modules available: PDF Parser, Audit Log, Member Rounds, and Settings."
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

        setContent(container);
        return container;
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

    /** Small pill badge used to annotate nav items (e.g. "NEW"). */
    private static Span buildBadge(String text) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add("badge small contrast");
        return badge;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        // Show the placeholder whenever the root route ("/") is active and
        // no child view has set content explicitly.
        if (getContent() == null) {
            buildModuleContainerPlaceholder();
        }
    }
}
