package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.theme.lumo.Lumo
import jakarta.annotation.security.PermitAll

@PermitAll
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("context://styles/global.css")
class AuthenticatedView(
    navbar: Navbar,
    statusBar: StatusBar
) : AppLayout(), LocaleChangeObserver {
    private val singleFileButton = Button().apply {
        addClassName("main-menu-action-button")
        addClickListener {
            ui.ifPresent { currentUi ->
                currentUi.navigate(MainView::class.java)
            }
            mainMenuPanel.isVisible = false
        }
    }
    private val workspaceButton = Button().apply {
        addClassName("main-menu-action-button")
        addClickListener {
            ui.ifPresent { currentUi ->
                currentUi.navigate(WorkspaceView::class.java)
            }
            mainMenuPanel.isVisible = false
        }
    }
    private val mainMenuPanel = HorizontalLayout(singleFileButton, workspaceButton).apply {
        addClassName("main-menu-panel")
        setWidthFull()
        isPadding = false
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        isVisible = false
    }
    private val viewContainer = Div().apply {
        setWidthFull()
    }

    init {
        refreshLocalizedText(AppMessages.resolveLocale())
        navbar.addAppMenuToggleListener {
            mainMenuPanel.isVisible = !mainMenuPanel.isVisible
        }

        addToNavbar(
            VerticalLayout(navbar, statusBar).apply {
                setWidthFull()
                isPadding = false
                isSpacing = false
                defaultHorizontalComponentAlignment = FlexComponent.Alignment.STRETCH
            }
        )

        content = VerticalLayout(mainMenuPanel, viewContainer).apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        viewContainer.removeAll()
        if (content is Component) {
            viewContainer.add(content)
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
    }

    private fun refreshLocalizedText(locale: java.util.Locale) {
        singleFileButton.text = AppMessages.translate(locale, "menu.singleFileValidation")
        workspaceButton.text = AppMessages.translate(locale, "menu.workspaceValidation")
    }
}
