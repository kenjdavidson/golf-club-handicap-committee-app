package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.file.ParserDefinition
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll

@Route(value = "settings", layout = AuthenticatedLayout::class)
@PageTitle("Settings | Handicap Committee App")
@PermitAll
class SettingsView(
    private val appSettings: UserSettingsService,
    private val roundParsers: List<RoundParser>
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()
    private val navigationTabs = Tabs()
    private val clubManagementTab = Tab()
    private val aiFeaturesTab = Tab()
    private val aboutTab = Tab()
    private val contentArea = VerticalLayout()

    private val parserTypeSelect = Select<RoundParser>()
    private val parserTypeNote = Paragraph()
    private val parserTypeDescription = Paragraph()
    private val maxRoundsField = IntegerField()
    private val maxRoundsDescription = Paragraph()
    private val defaultHomeCourseField = TextField()
    private val defaultHomeCourseDescription = Paragraph()

    private val aiFeaturesTitle = H3()
    private val aiFeaturesDescription = Paragraph()

    private val aboutTitle = H3()
    private val githubLink = Anchor("https://github.com/kenjdavidson/golf-club-handicap-committee-app", "")
    private val downloadLink = Anchor("https://github.com/kenjdavidson/golf-club-handicap-committee-app/releases", "")
    private val versionInfo = Paragraph()
    private val contactInfo = Paragraph()

    private val clubManagementContent = createSettingsPage(
        createSettingSection(parserTypeSelect, parserTypeNote, parserTypeDescription),
        createSettingSection(maxRoundsField, maxRoundsDescription),
        createSettingSection(defaultHomeCourseField, defaultHomeCourseDescription)
    )
    private val aiFeaturesContent = createSettingsPage(aiFeaturesTitle, aiFeaturesDescription)
    private val aboutContent = createSettingsPage(aboutTitle, githubLink, downloadLink, versionInfo, contactInfo)
    private val pageContentByTab = mapOf(
        clubManagementTab to clubManagementContent,
        aiFeaturesTab to aiFeaturesContent,
        aboutTab to aboutContent
    )

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        title.text = AppMessages.translateCurrent("settings.title")

        parserTypeSelect.setItems(roundParsers)
        parserTypeSelect.value = appSettings.selectedParser

        refreshParserTypeLabels()
        updateParserDescription(appSettings.selectedParser)

        parserTypeSelect.addValueChangeListener { event ->
            appSettings.selectedParser = event.value
            updateParserDescription(event.value)
        }

        maxRoundsField.isStepButtonsVisible = true
        maxRoundsField.min = 1
        maxRoundsField.setWidthFull()
        maxRoundsField.value = appSettings.maxRounds
        maxRoundsField.addValueChangeListener { event ->
            val value = event.value ?: appSettings.maxRounds
            appSettings.maxRounds = value
            if (value < 1) {
                maxRoundsField.value = appSettings.maxRounds
            }
        }

        parserTypeSelect.setWidthFull()
        defaultHomeCourseField.value = appSettings.defaultHomeCourse.orEmpty()
        defaultHomeCourseField.setWidthFull()
        defaultHomeCourseField.addValueChangeListener { event ->
            appSettings.defaultHomeCourse = event.value
        }

        navigationTabs.orientation = Tabs.Orientation.VERTICAL
        navigationTabs.add(clubManagementTab, aiFeaturesTab, aboutTab)
        navigationTabs.selectedTab = clubManagementTab
        navigationTabs.addSelectedChangeListener { event ->
            showTabContent(event.selectedTab ?: clubManagementTab)
        }

        contentArea.setSizeFull()
        contentArea.isPadding = false
        contentArea.isSpacing = false
        contentArea.style.set("gap", "var(--lumo-space-l)")

        val layout = HorizontalLayout(navigationTabs, contentArea).apply {
            setSizeFull()
            isPadding = false
            isSpacing = true
            setFlexGrow(1.0, contentArea)
            alignItems = FlexComponent.Alignment.START
        }

        navigationTabs.style.set("min-width", "14rem")
        navigationTabs.style.set("flex-shrink", "0")

        refreshMaxRoundsFieldLabels()
        refreshDefaultHomeCourseFieldLabels()
        refreshAdditionalText()
        showTabContent(clubManagementTab)

        add(title, layout)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        title.text = AppMessages.translate(event.locale, "settings.title")
        refreshParserTypeLabels()
        updateParserDescription(parserTypeSelect.value)
        refreshMaxRoundsFieldLabels()
        refreshDefaultHomeCourseFieldLabels()
        refreshAdditionalText()
        showTabContent(navigationTabs.selectedTab ?: clubManagementTab)
    }

    private fun refreshParserTypeLabels() {
        parserTypeSelect.label = AppMessages.translateCurrent("settings.parser.type.label")
        parserTypeSelect.setItemLabelGenerator { parser ->
            parser.javaClass.getAnnotation(ParserDefinition::class.java)
                ?.displayNameKey
                ?.let { AppMessages.translateCurrent(it) }
                ?: parser.javaClass.simpleName
        }
    }

    private fun updateParserDescription(parser: RoundParser?) {
        parserTypeDescription.text = parser
            ?.javaClass?.getAnnotation(ParserDefinition::class.java)
            ?.descriptionKey
            ?.let { AppMessages.translateCurrent(it) }
            ?: ""
    }

    private fun refreshDefaultHomeCourseFieldLabels() {
        defaultHomeCourseField.label = AppMessages.translateCurrent("settings.defaultHomeCourse.label")
        defaultHomeCourseField.placeholder = AppMessages.translateCurrent("settings.defaultHomeCourse.placeholder")
    }

    private fun refreshMaxRoundsFieldLabels() {
        maxRoundsField.label = AppMessages.translateCurrent("settings.maxRounds.label")
        maxRoundsField.helperText = AppMessages.translateCurrent("settings.maxRounds.helper")
    }

    private fun refreshAdditionalText() {
        clubManagementTab.label = AppMessages.translateCurrent("settings.menu.clubManagement")
        aiFeaturesTab.label = AppMessages.translateCurrent("settings.menu.aiFeatures")
        aboutTab.label = AppMessages.translateCurrent("settings.menu.about")

        parserTypeNote.text = AppMessages.translateCurrent("settings.parser.type.note")
        maxRoundsDescription.text = AppMessages.translateCurrent("settings.maxRounds.description")
        defaultHomeCourseDescription.text = AppMessages.translateCurrent("settings.defaultHomeCourse.description")

        aiFeaturesTitle.text = AppMessages.translateCurrent("settings.menu.aiFeatures")
        aiFeaturesDescription.text = AppMessages.translateCurrent("settings.aiFeatures.empty")

        aboutTitle.text = AppMessages.translateCurrent("settings.menu.about")
        githubLink.text = AppMessages.translateCurrent("settings.about.github")
        downloadLink.text = AppMessages.translateCurrent("settings.about.download")
        versionInfo.text = "${AppMessages.translateCurrent("settings.about.version")}: ${currentVersion()}"
        contactInfo.text = AppMessages.translateCurrent("settings.about.contact")
    }

    private fun showTabContent(selectedTab: Tab) {
        contentArea.removeAll()
        contentArea.add(pageContentByTab[selectedTab] ?: clubManagementContent)
    }

    private fun createSettingSection(
        input: Component,
        description: Paragraph,
        vararg extraComponents: Component
    ): VerticalLayout {
        return VerticalLayout().apply {
            configureSectionContainer(this)
            add(description, input, *extraComponents)
        }
    }

    private fun createSettingsPage(vararg components: Component): VerticalLayout {
        return VerticalLayout().apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
            style.set("gap", "var(--lumo-space-l)")
            add(*components)
        }
    }

    private fun configureSectionContainer(section: VerticalLayout) {
        section.setWidthFull()
        section.isPadding = false
        section.isSpacing = false
        section.style.set("gap", "var(--lumo-space-s)")
        section.style.set("max-width", "36rem")
    }

    private fun currentVersion(): String {
        return javaClass.`package`?.implementationVersion ?: "development"
    }
}
