package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "workspace", layout = AuthenticatedLayout::class)
@PageTitle("Workspace Validation | Handicap Committee App")
@PermitAll
class WorkspaceView : VerticalLayout() {
    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
    }
}
