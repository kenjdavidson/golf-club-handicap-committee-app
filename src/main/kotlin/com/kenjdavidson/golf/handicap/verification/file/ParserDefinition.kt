package com.kenjdavidson.golf.handicap.verification.file

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParserDefinition(
    val displayNameKey: String,
    val descriptionKey: String
)
