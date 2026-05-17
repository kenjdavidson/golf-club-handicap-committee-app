package com.kenjdavidson.golf.handicap.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Login | Handicap Committee App")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        getStyle().set("justify-content", "center");
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        H1 heading = new H1("⛳ Handicap Committee App");
        heading.addClassNames(LumoUtility.Margin.Bottom.XSMALL);

        Paragraph message = new Paragraph("Sign in to access the committee workspace.");
        message.addClassNames(
            LumoUtility.Margin.Top.NONE,
            LumoUtility.TextColor.SECONDARY
        );

        loginForm.setAction("login");

        VerticalLayout card = new VerticalLayout(heading, message, loginForm);
        card.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.setSpacing(true);
        card.setPadding(true);
        card.setMaxWidth("420px");
        card.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-s)");

        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loginForm.setError(event.getLocation()
            .getQueryParameters()
            .getParameters()
            .containsKey("error"));
    }
}
