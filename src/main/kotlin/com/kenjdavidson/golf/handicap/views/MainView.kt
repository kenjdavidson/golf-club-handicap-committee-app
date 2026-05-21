package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll

@Route(value = "", layout = AuthenticatedView::class)
@PageTitle("Handicap Committee App")
@PermitAll
class MainView(
    private val authenticationContext: AuthenticationContext,
    private val userProfileResolver: UserProfileResolver,
    private val singleFileVerificationCardFactory: SingleFileVerificationCardFactory
) : VerticalLayout() {

    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
        style["overflow"] = "auto"
        style["min-height"] = "0"
        element.setAttribute("tabindex", "0")
        element.setAttribute("aria-label", "Main content")
        add(singleFileVerificationCardFactory.create(authenticatedUser))
    }
}
