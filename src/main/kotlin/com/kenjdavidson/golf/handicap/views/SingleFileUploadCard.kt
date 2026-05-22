package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.Upload
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

    private val upload = Upload(
        UploadHandler.inMemory { metadata, data ->
            uploadedBytes = data
            uploadedFileName = metadata.fileName()
            fileName.text = metadata.fileName()
            syncVerifyEnabled()
            fileSelectedListener?.invoke(metadata.fileName())
        }
    )

    private val verifyButton = Button("Verify", VaadinIcon.CHECK.create()).apply {
        isEnabled = false
        addClickListener {
            val fileBytes = uploadedBytes ?: return@addClickListener
            val name = uploadedFileName ?: return@addClickListener
            verifyHandler?.invoke(name, fileBytes)
        }
    }

    init {
        upload.apply {
            setAcceptedFileTypes(".pdf")
            isAutoUpload = true
            setDropAllowed(false)
            setUploadButton(Button("Upload file", VaadinIcon.UPLOAD.create()))
            setDropLabel(Span())
            setDropLabelIcon(Span())
            style["padding"] = "0"
            style["border"] = "none"
            style["min-height"] = "0"
            style["background"] = "transparent"
        }

        upload.addFileRejectedListener { event ->
            clearFile()
            fileRejectedListener?.invoke(event.errorMessage)
        }

        upload.addFileRemovedListener {
            clearFile()
        }

        val uploadPanel = Div(fileName, upload).apply {
            setWidthFull()
            style["display"] = "flex"
            style["align-items"] = "center"
            style["gap"] = "var(--lumo-space-s)"
            style["padding"] = "var(--lumo-space-s) var(--lumo-space-m)"
            style["border"] = "1px dotted var(--lumo-primary-color-50pct)"
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
    }
}
