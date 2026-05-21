package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer

class SingleFileUploadCard : HorizontalLayout() {
    private val uploadBuffer = MemoryBuffer()
    private var uploadedBytes: ByteArray? = null
    private var uploadedFileName: String? = null
    private var verifyHandler: ((String, ByteArray) -> Unit)? = null
    private var fileSelectedListener: ((String) -> Unit)? = null
    private var fileRejectedListener: ((String) -> Unit)? = null

    private val fileName = Span("No file selected").apply {
        style["white-space"] = "normal"
        style["word-break"] = "break-word"
        style["flex-grow"] = "1"
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
        val upload = Upload(uploadBuffer).apply {
            setAcceptedFileTypes(".pdf")
            isAutoUpload = true
            setDropAllowed(false)
            setUploadButton(Button("Upload file", VaadinIcon.UPLOAD.create()))
            style["padding"] = "0"
            style["border"] = "none"
            style["min-height"] = "0"
        }

        upload.addSucceededListener { event ->
            uploadedBytes = uploadBuffer.inputStream.readBytes()
            uploadedFileName = event.fileName
            fileName.text = event.fileName
            syncVerifyEnabled()
            fileSelectedListener?.invoke(event.fileName)
        }

        upload.addFileRejectedListener { event ->
            clearFile()
            fileRejectedListener?.invoke(event.errorMessage)
        }

        val uploadPanel = HorizontalLayout(fileName, upload).apply {
            setWidthFull()
            isPadding = true
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style["border"] = "1px dotted var(--lumo-primary-color-50pct)"
            style["border-radius"] = "var(--lumo-border-radius-m)"
            style["background"] = "var(--lumo-primary-color-10pct)"
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
        fileName.text = "No file selected"
        syncVerifyEnabled()
    }

    private fun syncVerifyEnabled() {
        verifyButton.isEnabled = verifyHandler != null && uploadedBytes?.isNotEmpty() == true
    }
}
