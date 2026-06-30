package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.UploadButton
import com.vaadin.flow.component.upload.UploadManager
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.server.streams.UploadHandler
import java.util.Locale

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

    private val uploadManager = UploadManager(this, UploadHandler { event ->
        try {
            val bytes = event.inputStream.readBytes()
            event.ui.access {
                uploadedBytes = bytes
                uploadedFileName = event.fileName
                fileName.text = event.fileName
                fileSelectedListener?.invoke(event.fileName)
                verifyIfReady()
            }
        } catch (exception: Exception) {
            val message = "${AppMessages.translateCurrent("upload.failed")}: ${exception.message ?: AppMessages.translateCurrent("upload.unknownError")}"
            event.reject(message)
            event.ui.access {
                clearFile()
                fileRejectedListener?.invoke(message)
            }
        }
    }).apply {
        setMaxFiles(1)
        setAcceptedMimeTypes("application/pdf")
        setAcceptedFileExtensions(".pdf")
        setMaxFileSize(25L * 1024 * 1024)
    }

    private val upload = UploadButton(uploadManager).apply {
        text = AppMessages.translateCurrent("upload.button")
    }

    init {
        val uploadPanel = HorizontalLayout(fileName, upload).apply {
            setWidthFull()
            isPadding = true
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style["background"] = "var(--lumo-primary-color-10pct)"
            style["box-sizing"] = "border-box"
        }

        add(uploadPanel)
        setWidthFull()
        isPadding = false
        isSpacing = true
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(uploadPanel)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        refreshLocalizedText(event.locale)
    }

    fun setVerifyHandler(handler: (String, ByteArray) -> Unit) {
        verifyHandler = handler
        verifyIfReady()
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
    }

    private fun verifyIfReady() {
        val handler = verifyHandler ?: return
        val name = uploadedFileName ?: return
        val fileBytes = uploadedBytes ?: return
        if (fileBytes.isEmpty()) {
            return
        }
        handler.invoke(name, fileBytes)
    }

    private fun refreshLocalizedText(locale: Locale) {
        upload.text = AppMessages.translate(locale, "upload.button")
        if (uploadedFileName == null) {
            fileName.text = AppMessages.translate(locale, "upload.noFileSelected")
        }
    }
}
