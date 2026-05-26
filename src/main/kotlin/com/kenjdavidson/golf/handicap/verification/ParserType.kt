package com.kenjdavidson.golf.handicap.verification

enum class ParserType(
    val displayNameKey: String,
    val descriptionKey: String
) {
    PDF_TYPE_1(
        displayNameKey = "settings.parser.type.pdfType1.name",
        descriptionKey = "settings.parser.type.pdfType1.description"
    ),
    PDF_TYPE_2(
        displayNameKey = "settings.parser.type.pdfType2.name",
        descriptionKey = "settings.parser.type.pdfType2.description"
    ),
    CSV_TYPE_3(
        displayNameKey = "settings.parser.type.csvType3.name",
        descriptionKey = "settings.parser.type.csvType3.description"
    )
}
