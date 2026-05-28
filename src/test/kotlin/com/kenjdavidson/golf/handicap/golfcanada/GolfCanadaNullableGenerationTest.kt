package com.kenjdavidson.golf.handicap.golfcanada

import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GolfCanadaNullableGenerationTest {
    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()

    @Test
    fun `generated history entry nullable fields do not use json nullable wrapper`() {
        val getScaleTypeId = HistoryEntry::class.java.getMethod("getScaleTypeId")
        val getPlayedDifferential = HistoryEntry::class.java.getMethod("getPlayedDifferential")
        val getExpectedDifferential = HistoryEntry::class.java.getMethod("getExpectedDifferential")

        assertEquals(String::class.java, getScaleTypeId.returnType)
        assertEquals(Double::class.javaObjectType, getPlayedDifferential.returnType)
        assertEquals(Double::class.javaObjectType, getExpectedDifferential.returnType)
        assertFalse(getScaleTypeId.returnType.name.contains("JsonNullable"))
        assertFalse(getPlayedDifferential.returnType.name.contains("JsonNullable"))
        assertFalse(getExpectedDifferential.returnType.name.contains("JsonNullable"))
    }

    @Test
    fun `profile payload with nulls and club object deserializes`() {
        val profileJson = """
            {
              "individualId": 11111111,
              "cardId": "111111111",
              "name": "Test Member",
              "handicap": "7.3",
              "level": "Gold",
              "city": null,
              "region": null,
              "club": {
                "name": "Blue Springs Golf Club",
                "line1": "13448 Dublin Line",
                "line2": null,
                "city": "Acton",
                "region": "ON",
                "phone": "(519) 853-0904",
                "url": "http://bluesprings.clublink.ca/",
                "logo@odata.mediaReadLink": "/uploads/Club/a2cf015335054e2c867905cbf8e0f730.jpg"
              }
            }
        """.trimIndent()

        val profile = objectMapper.readValue(profileJson, Profile::class.java)

        assertEquals(11111111L, requireNotNull(profile.individualId))
        assertEquals("Blue Springs Golf Club", profile.club?.name)
        assertNull(profile.club?.line2)
    }

    @Test
    fun `history payload scale type id string deserializes`() {
        val historyEntryJson = """
            {
              "id": 1,
              "course": "Some Course",
              "scaleTypeId": "FrontNine",
              "playedDifferential": null
            }
        """.trimIndent()

        val historyEntry = objectMapper.readValue(historyEntryJson, HistoryEntry::class.java)

        assertNotNull(historyEntry)
        assertEquals("FrontNine", historyEntry.scaleTypeId)
        assertNull(historyEntry.playedDifferential)
    }
}
