package com.kenjdavidson.golf.handicap.golfcanada.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class ProfileClubDeserializer : JsonDeserializer<ProfileClub?>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ProfileClub? {
        return when (parser.currentToken()) {
            JsonToken.VALUE_STRING -> ProfileClub().name(parser.valueAsString)
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> deserializeObject(parser)
            else -> context.reportInputMismatch(
                ProfileClub::class.java,
                "Expected club as string or object but got %s",
                parser.currentToken()
            )
        }
    }

    private fun deserializeObject(parser: JsonParser): ProfileClub {
        val node = parser.codec.readTree<JsonNode>(parser)
        return ProfileClub()
            .name(node["name"]?.textValue())
            .line1(node["line1"]?.textValue())
            .line2(node["line2"]?.textValue())
            .city(node["city"]?.textValue())
            .region(node["region"]?.textValue())
            .phone(node["phone"]?.textValue())
            .url(node["url"]?.textValue())
            .logoAtOdataMediaReadLink(node["logo@odata.mediaReadLink"]?.textValue())
    }
}
