package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.ai.AiIntegrationType
import com.kenjdavidson.golf.handicap.ai.AiSettingsService
import com.kenjdavidson.golf.handicap.ai.ModelDownloadState
import com.kenjdavidson.golf.handicap.ai.OllamaModelDownloadService
import com.kenjdavidson.golf.handicap.ai.OllamaModelOption
import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.file.ParserDefinition
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.PasswordField
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
    private val aiSettingsService: AiSettingsService,
    private val downloadService: OllamaModelDownloadService,
    private val roundParsers: List<RoundParser>
) : VerticalLayout(), LocaleChangeObserver {

    private val title = H2()
    private val navigationTabs = Tabs()
    private val clubManagementTab = Tab()
    private val aiFeaturesTab = Tab()
    private val aboutTab = Tab()
    private val contentArea = VerticalLayout()

    // ── Club Management controls ───────────────────────────────────────────────
    private val parserTypeSelect = Select<RoundParser>()
    private val parserTypeNote = Paragraph()
    private val parserTypeDescription = Paragraph()
    private val maxRoundsField = IntegerField()
    private val maxRoundsDescription = Paragraph()
    private val defaultHomeCourseField = TextField()
    private val defaultHomeCourseDescription = Paragraph()

    // ── AI Features controls ───────────────────────────────────────────────────
    private val aiIntegrationTypeSelect = Select<AiIntegrationType>()
    private val aiIntegrationTypeDescription = Paragraph()
    private val aiModelSelect = Select<OllamaModelOption>()
    private val aiModelDescription = Paragraph()
    private val aiModelDownloadButton = Button()
    private val aiModelProgressBar = ProgressBar().apply { isIndeterminate = true; isVisible = false }
    private val aiModelProgressLabel = Span().apply { isVisible = false }
    private val aiModelSection = VerticalLayout()
    private val aiGeminiApiKeyField = PasswordField()
    private val aiGeminiApiKeyDescription = Paragraph()
    private val aiGeminiSection = VerticalLayout()

    // ── About controls ─────────────────────────────────────────────────────────
    private val aboutTitle = H3()
    private val githubLink = Anchor("https://github.com/kenjdavidson/golf-club-handicap-committee-app", "")
    private val downloadLink = Anchor("https://github.com/kenjdavidson/golf-club-handicap-committee-app/releases", "")
    private val versionInfo = Paragraph()
    private val contactInfo = Paragraph()

    /** Listener registered with [OllamaModelDownloadService] – removed on detach. */
    private var downloadListener: OllamaModelDownloadService.StateChangeListener? = null

    private val clubManagementContent = createSettingsPage(
        createSettingSection(parserTypeSelect, parserTypeNote, parserTypeDescription),
        createSettingSection(maxRoundsField, maxRoundsDescription),
        createSettingSection(defaultHomeCourseField, defaultHomeCourseDescription)
    )
    private val aboutContent = createSettingsPage(aboutTitle, githubLink, downloadLink, versionInfo, contactInfo)
    private lateinit var aiFeaturesContent: VerticalLayout
    private lateinit var pageContentByTab: Map<Tab, VerticalLayout>

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        title.text = AppMessages.translateCurrent("settings.title")

        // ── Club Management ────────────────────────────────────────────────────
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
            if (value < 1) maxRoundsField.value = appSettings.maxRounds
        }

        parserTypeSelect.setWidthFull()
        defaultHomeCourseField.value = appSettings.defaultHomeCourse.orEmpty()
        defaultHomeCourseField.setWidthFull()
        defaultHomeCourseField.addValueChangeListener { event ->
            appSettings.defaultHomeCourse = event.value
        }

        // ── AI Features ────────────────────────────────────────────────────────
        setupAiIntegrationTypeSelect()
        setupAiModelSelect()
        setupAiModelDownloadButton()
        setupGeminiApiKeyField()
        aiFeaturesContent = buildAiFeaturesPage()
        pageContentByTab = mapOf(
            clubManagementTab to clubManagementContent,
            aiFeaturesTab to aiFeaturesContent,
            aboutTab to aboutContent
        )

        // ── Navigation ─────────────────────────────────────────────────────────
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

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        if (aiSettingsService.integrationType != AiIntegrationType.EXTERNAL &&
            aiSettingsService.integrationType != AiIntegrationType.LOCAL
        ) return
        val currentModel = aiSettingsService.selectedModelTag ?: return
        val ui = attachEvent.ui
        val listener = OllamaModelDownloadService.StateChangeListener { modelTag, state ->
            ui.access { applyDownloadState(modelTag, state) }
        }
        downloadListener = listener
        downloadService.addListener(currentModel, listener)
    }

    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        if (aiSettingsService.integrationType != AiIntegrationType.EXTERNAL &&
            aiSettingsService.integrationType != AiIntegrationType.LOCAL
        ) return
        val tag = aiSettingsService.selectedModelTag ?: return
        downloadListener?.let { downloadService.removeListener(tag, it) }
        downloadListener = null
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

    // ── AI Features setup ──────────────────────────────────────────────────────

    private fun setupAiIntegrationTypeSelect() {
        aiIntegrationTypeSelect.setItems(AiIntegrationType.entries.toList())
        aiIntegrationTypeSelect.value = aiSettingsService.integrationType
        aiIntegrationTypeSelect.setItemLabelGenerator { type ->
            AppMessages.translateCurrent("ai.integration.${type.name.lowercase()}.label")
        }
        aiIntegrationTypeSelect.setWidthFull()
        aiIntegrationTypeSelect.addValueChangeListener { event ->
            val type = event.value ?: AiIntegrationType.NONE
            aiSettingsService.integrationType = type
            appSettings.persistSettings()
            updateAiIntegrationDescription(type)
            aiModelSection.isVisible = type == AiIntegrationType.EXTERNAL || type == AiIntegrationType.LOCAL
            aiGeminiSection.isVisible = type == AiIntegrationType.GEMINI
        }
        updateAiIntegrationDescription(aiSettingsService.integrationType)
    }

    private fun setupAiModelSelect() {
        aiModelSelect.setItems(OllamaModelOption.ALL)
        aiModelSelect.setItemLabelGenerator { model ->
            "${AppMessages.translateCurrent(model.displayNameKey)} (~${model.estimatedSizeGb} GB)"
        }
        aiModelSelect.setWidthFull()

        val currentTag = aiSettingsService.selectedModelTag
        if (currentTag != null) {
            aiModelSelect.value = OllamaModelOption.ALL.find { it.tag == currentTag }
        }

        aiModelSelect.addValueChangeListener { event ->
            val model = event.value ?: return@addValueChangeListener
            // Re-register download listener for the newly selected model
            val currentUi = ui.orElse(null) ?: return@addValueChangeListener
            downloadListener?.let { l ->
                aiSettingsService.selectedModelTag?.let { oldTag ->
                    downloadService.removeListener(oldTag, l)
                }
            }
            aiSettingsService.selectedModelTag = model.tag
            appSettings.persistSettings()
            updateAiModelDescription(model)

            val newListener = OllamaModelDownloadService.StateChangeListener { modelTag, state ->
                currentUi.access { applyDownloadState(modelTag, state) }
            }
            downloadListener = newListener
            downloadService.addListener(model.tag, newListener)
        }
        if (aiModelSelect.value != null) updateAiModelDescription(aiModelSelect.value)
    }

    private fun setupAiModelDownloadButton() {
        aiModelDownloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        aiModelDownloadButton.addClickListener {
            val model = aiModelSelect.value ?: return@addClickListener
            val currentState = downloadService.getState(model.tag)
            when (currentState) {
                is ModelDownloadState.Downloading -> downloadService.cancelDownload(model.tag)
                else -> downloadService.startDownload(model.tag)
            }
        }
    }

    private fun setupGeminiApiKeyField() {
        aiGeminiApiKeyField.setWidthFull()
        aiGeminiApiKeyField.isRevealButtonVisible = false
        aiGeminiApiKeyField.value = aiSettingsService.geminiApiKey.orEmpty()
        aiGeminiApiKeyField.addValueChangeListener { event ->
            aiSettingsService.geminiApiKey = event.value
            appSettings.persistSettings()
        }
    }

    private fun buildAiFeaturesPage(): VerticalLayout {
        aiModelSection.apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
            style.set("gap", "var(--lumo-space-s)")
            style.set("max-width", "36rem")
            isVisible = aiSettingsService.integrationType == AiIntegrationType.EXTERNAL ||
                aiSettingsService.integrationType == AiIntegrationType.LOCAL

            val progressRow = HorizontalLayout(aiModelProgressBar, aiModelProgressLabel).apply {
                setWidthFull()
                isPadding = false
                isSpacing = true
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                aiModelProgressBar.setWidthFull()
            }

            add(aiModelDescription, aiModelSelect, progressRow, aiModelDownloadButton)
        }

        aiGeminiSection.apply {
            setWidthFull()
            isPadding = false
            isSpacing = false
            style.set("gap", "var(--lumo-space-s)")
            style.set("max-width", "36rem")
            isVisible = aiSettingsService.integrationType == AiIntegrationType.GEMINI
            add(aiGeminiApiKeyDescription, aiGeminiApiKeyField)
        }

        return createSettingsPage(
            createSettingSection(aiIntegrationTypeSelect, aiIntegrationTypeDescription, aiModelSection, aiGeminiSection)
        )
    }

    private fun applyDownloadState(modelTag: String, state: ModelDownloadState) {
        if (aiModelSelect.value?.tag != modelTag) return

        when (state) {
            is ModelDownloadState.Idle -> {
                aiModelProgressBar.isVisible = false
                aiModelProgressLabel.isVisible = false
                aiModelDownloadButton.text = AppMessages.translateCurrent("ai.model.action.download")
                aiModelDownloadButton.setIcon(VaadinIcon.DOWNLOAD.create())
                aiModelDownloadButton.removeThemeVariants(ButtonVariant.LUMO_ERROR)
                aiModelDownloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
            is ModelDownloadState.Downloading -> {
                aiModelProgressBar.isVisible = true
                aiModelProgressLabel.isVisible = true
                aiModelProgressLabel.text = state.statusMessage
                if (state.progress < 0) {
                    aiModelProgressBar.isIndeterminate = true
                } else {
                    aiModelProgressBar.isIndeterminate = false
                    aiModelProgressBar.value = state.progress
                }
                aiModelDownloadButton.text = AppMessages.translateCurrent("ai.model.action.cancel")
                aiModelDownloadButton.setIcon(VaadinIcon.CLOSE.create())
                aiModelDownloadButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                aiModelDownloadButton.addThemeVariants(ButtonVariant.LUMO_ERROR)
            }
            is ModelDownloadState.Complete -> {
                aiModelProgressBar.isVisible = false
                aiModelProgressLabel.isVisible = false
                aiModelDownloadButton.text = AppMessages.translateCurrent("ai.model.action.ready")
                aiModelDownloadButton.setIcon(VaadinIcon.CHECK.create())
                aiModelDownloadButton.removeThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY)
                aiModelDownloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS)

                val notification = Notification.show(
                    AppMessages.translateCurrent("ai.model.download.complete", modelTag),
                    4000,
                    Notification.Position.BOTTOM_END
                )
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            }
            is ModelDownloadState.Failed -> {
                aiModelProgressBar.isVisible = false
                aiModelProgressLabel.isVisible = false
                aiModelDownloadButton.text = AppMessages.translateCurrent("ai.model.action.retry")
                aiModelDownloadButton.setIcon(VaadinIcon.REFRESH.create())
                aiModelDownloadButton.removeThemeVariants(ButtonVariant.LUMO_SUCCESS)
                aiModelDownloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)

                val notification = Notification.show(
                    AppMessages.translateCurrent("ai.model.download.failed", state.reason),
                    6000,
                    Notification.Position.BOTTOM_END
                )
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        }
    }

    // ── Label refresh helpers ──────────────────────────────────────────────────

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

    private fun updateAiIntegrationDescription(type: AiIntegrationType) {
        aiIntegrationTypeDescription.text =
            AppMessages.translateCurrent("ai.integration.${type.name.lowercase()}.description")
    }

    private fun updateAiModelDescription(model: OllamaModelOption?) {
        aiModelDescription.text = model
            ?.let { AppMessages.translateCurrent(it.descriptionKey) }
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

        aiIntegrationTypeSelect.label = AppMessages.translateCurrent("ai.integration.type.label")
        aiModelSelect.label = AppMessages.translateCurrent("ai.model.label")
        aiGeminiApiKeyField.label = AppMessages.translateCurrent("ai.gemini.apiKey.label")
        aiGeminiApiKeyField.placeholder = AppMessages.translateCurrent("ai.gemini.apiKey.placeholder")
        aiGeminiApiKeyDescription.text = AppMessages.translateCurrent("ai.gemini.apiKey.description")
        aiModelDownloadButton.text = when (
            aiSettingsService.selectedModelTag?.let { downloadService.getState(it) }
        ) {
            is ModelDownloadState.Downloading -> AppMessages.translateCurrent("ai.model.action.cancel")
            is ModelDownloadState.Complete -> AppMessages.translateCurrent("ai.model.action.ready")
            else -> AppMessages.translateCurrent("ai.model.action.download")
        }

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
