package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.Navbar
import com.kenjdavidson.golf.handicap.components.StatusBar
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.Footer
import com.vaadin.flow.component.html.Header
import com.vaadin.flow.component.html.Main
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.theme.lumo.Lumo
import com.vaadin.flow.theme.lumo.LumoUtility
import jakarta.annotation.security.PermitAll

@PermitAll
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("context://styles/global.css")
class AuthenticatedLayout(
    private val navbar: Navbar,
    private val statusBar: StatusBar
) : FlexLayout(), RouterLayout, LocaleChangeObserver {

    private val appHeader = Header()
    private val appMainContent = Main()
    private val appFooter = Footer()

    private val singleFileButton = Button().apply {
        addClassName("main-menu-action-button")
        style["cursor"] = "pointer"
    }

    private val workspaceButton = Button().apply {
        addClassName("main-menu-action-button")
        style["cursor"] = "pointer"
    }

    private val mainMenuPanel = HorizontalLayout(singleFileButton, workspaceButton).apply {
        addClassName("main-menu-panel")
        setWidthFull()
        isPadding = false
        isSpacing = true
        isVisible = false

        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.SMALL
        )
    }

    init {
        // 1. Setup the outer shell container sizing and stacking direction
        setId("app-shell-container")
        setSizeFull()
        flexDirection = FlexLayout.FlexDirection.COLUMN

        // 2. Class names for custom sticky header/footer scrolling hooks
        appHeader.addClassName("app-header")
        appMainContent.addClassName("app-main-content")
        appFooter.addClassName("app-footer")

        // 3. Assemble components inside the Header wrapper
        val headerLayout = VerticalLayout().apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
            add(navbar, mainMenuPanel)
        }
        appHeader.add(headerLayout)

        // 4. Assemble components inside the Footer wrapper
        appFooter.add(statusBar)

        // 5. Build layout triggers and localize button items
        refreshLocalizedText(AppMessages.resolveLocale())

        singleFileButton.addClickListener {
            ui.ifPresent { currentUi ->
                currentUi.navigate(MainView::class.java)
            }
            mainMenuPanel.isVisible = false
        }

        workspaceButton.addClickListener {
            ui.ifPresent { currentUi ->
                currentUi.navigate(WorkspaceView::class.java)
            }
            mainMenuPanel.isVisible = false
        }

        navbar.addAppMenuToggleListener {
            mainMenuPanel.isVisible = !mainMenuPanel.isVisible
        }

        // Add layout slots to our top-level container structure
        add(appHeader, appMainContent, appFooter)
    }

    // This handles swapping view fragments cleanly inside our scrollable content area
    override fun showRouterLayoutContent(content: HasElement?) {
        appMainContent.element.removeAllChildren()
        if (content != null) {
            appMainContent.element.appendChild(content.element)
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