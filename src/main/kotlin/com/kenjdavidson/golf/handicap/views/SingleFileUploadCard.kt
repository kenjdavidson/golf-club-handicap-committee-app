package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.UploadButton
import com.vaadin.flow.component.upload.UploadManager
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.server.streams.UploadHandler

class SingleFileUploadCard : HorizontalLayout(), LocaleChangeObserver {
    private var uploadedBytes: ByteArray? = null
    private var uploadedFileName: String? = null
    private var verifyHandler: ((String, ByteArray) -> Unit)? = null
    private var fileSelectedListener: ((String) -> Unit)? = null
    private var fileRejectedListener: ((String) -> Unit)? = null

    private val fileName = Span(AppMessages.translateCurrent("upload.noFileSelected")).apply {
        style["white-space"] = "normal"
        style["word-break"] = "break-word"
        style["flex-grow"] = "1"
    }

    private val upload = UploadButton(
        UploadManager(this, UploadHandler { event ->
            try {
                val bytes = event.inputStream.readBytes()
                event.ui.access {
                    uploadedBytes = bytes
                    uploadedFileName = event.fileName
                    fileName.text = event.fileName
                    syncVerifyEnabled()
                    fileSelectedListener?.invoke(event.fileName)
                }
            } catch (exception: Exception) {
                val message = "${AppMessages.translateCurrent("upload.failed")}: ${exception.message ?: AppMessages.translateCurrent("upload.unknownError")}"
                event.reject(message)
                event.ui.access {
                    clearFile()
                    fileRejectedListener?.invoke(message)
                }
            }
        })
    ).apply {
        text = AppMessages.translateCurrent("upload.button")
    }

    private val verifyButton = Button(AppMessages.translateCurrent("upload.verify"), VaadinIcon.CHECK.create()).apply {
        isEnabled = false
        addClickListener {
            val fileBytes = uploadedBytes ?: return@addClickListener
            val name = uploadedFileName ?: return@addClickListener
            verifyHandler?.invoke(name, fileBytes)
        }
    }

    init {
        val uploadPanel = HorizontalLayout(fileName, upload).apply {
            setWidthFull()
            isPadding = true
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style["border-width"] = "1px"
            style["border-style"] = "dashed"
            style["border-color"] = "var(--lumo-primary-color-50pct)"
            style["border-radius"] = "var(--lumo-border-radius-m)"
            style["background"] = "var(--lumo-primary-color-10pct)"
            style["box-sizing"] = "border-box"
        }

        add(uploadPanel, verifyButton)
        setWidthFull()
        isPadding = false
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(uploadPanel)
        refreshLocalizedText()
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
    }

    fun setVerifyHandler(handler: (String, ByteArray) -> Unit) {
        verifyHandler = handler
        syncVerifyEnabled()
    }

    fun setVerifyButtonLabel(label: String) {
        verifyButton.text = label
    }

    fun setFileSelectedListener(listener: (String) -> Unit) {
        fileSelectedListener = listener
    }

    fun setFileRejectedListener(listener: (String) -> Unit) {
        fileRejectedListener = listener
    }

    fun clearFile() {
        uploadedBytes = null
        uploadedFileName = null
        fileName.text = AppMessages.translateCurrent("upload.noFileSelected")
        syncVerifyEnabled()
    }

    private fun syncVerifyEnabled() {
        verifyButton.isEnabled = verifyHandler != null && uploadedBytes?.isNotEmpty() == true
    }

    private fun refreshLocalizedText(locale: java.util.Locale? = null) {
        upload.text = AppMessages.translate(locale, "upload.button")
        verifyButton.text = AppMessages.translate(locale, "upload.verify")
        if (uploadedFileName == null) {
            fileName.text = AppMessages.translate(locale, "upload.noFileSelected")
        }
    }
}
