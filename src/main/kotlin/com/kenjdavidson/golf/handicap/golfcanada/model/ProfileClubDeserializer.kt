package com.kenjdavidson.golf.handicap.golfcanada.model

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.node.ObjectNode

class ProfileClubDeserializer : ValueDeserializer<ProfileClub>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): ProfileClub? {
        return when (parser.currentToken()) {
            JsonToken.VALUE_STRING -> ProfileClub().name(parser.valueAsString)
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> deserializeObject(parser, context)
            else -> context.handleUnexpectedToken(ProfileClub::class.java, parser) as ProfileClub?
        }
    }

    private fun deserializeObject(parser: JsonParser, context: DeserializationContext): ProfileClub {
        // Fix 1: In Jackson 3, read the tree from the context instance instead of parser.codec
        val node = context.readTree(parser) as ObjectNode

        // Fix 2: Use .path() or safe null checks with .asText(null) to cleanly map string values
        return ProfileClub()
            .name(node.get("name")?.asString(null))
            .line1(node.get("line1")?.asString(null))
            .line2(node.get("line2")?.asString(null))
            .city(node.get("city")?.asString(null))
            .region(node.get("region")?.asString(null))
            .phone(node.get("phone")?.asString(null))
            .url(node.get("url")?.asString(null))
            .logoAtOdataMediaReadLink(node.get("logo@odata.mediaReadLink")?.asString(null))
    }
}