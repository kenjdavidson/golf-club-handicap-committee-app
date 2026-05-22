package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.UploadButton
import com.vaadin.flow.component.upload.UploadManager
import com.vaadin.flow.server.streams.UploadHandler

class SingleFileUploadCard : HorizontalLayout() {
    private var uploadedBytes: ByteArray? = null
    private var uploadedFileName: String? = null
    private var verifyHandler: ((String, ByteArray) -> Unit)? = null
    private var fileSelectedListener: ((String) -> Unit)? = null
    private var fileRejectedListener: ((String) -> Unit)? = null

    private val fileName = Span(NO_FILE_SELECTED).apply {
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
                val message = "$UPLOAD_FAILED: ${exception.message ?: UNKNOWN_ERROR}"
                event.reject(message)
                event.ui.access {
                    clearFile()
                    fileRejectedListener?.invoke(message)
                }
            }
        })
    ).apply {
        text = "Upload file"
    }

    private val verifyButton = Button("Verify", VaadinIcon.CHECK.create()).apply {
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
        fileName.text = NO_FILE_SELECTED
        syncVerifyEnabled()
    }

    private fun syncVerifyEnabled() {
        verifyButton.isEnabled = verifyHandler != null && uploadedBytes?.isNotEmpty() == true
    }

    private companion object {
        const val NO_FILE_SELECTED = "No file selected"
        const val UPLOAD_FAILED = "Upload failed"
        const val UNKNOWN_ERROR = "Unknown error"
    }
}
