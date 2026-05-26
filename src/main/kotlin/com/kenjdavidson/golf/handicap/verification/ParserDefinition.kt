package com.kenjdavidson.golf.handicap.verification

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParserDefinition(
    val displayNameKey: String,
    val descriptionKey: String
)
