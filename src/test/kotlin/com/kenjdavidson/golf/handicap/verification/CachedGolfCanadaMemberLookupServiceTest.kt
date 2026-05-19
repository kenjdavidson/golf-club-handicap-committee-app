package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CachedGolfCanadaMemberLookupServiceTest {
    private val membersApiFactory = mock(GolfCanadaMembersApiFactory::class.java)
    private val membersApi = mock(MembersApi::class.java)
    private val service = CachedGolfCanadaMemberLookupService(membersApiFactory)

    @Test
    fun `findMember returns match from parsed member id`() {
        `when`(membersApiFactory.create()).thenReturn(membersApi)
        `when`(membersApi.getProfile(152314L)).thenReturn(Profile().homeCourse("Snake Point Golf Club"))

        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "Ellis, Dean",
                memberId = "152314",
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertEquals(152314L, match?.individualId)
        assertEquals("Ellis, Dean", match?.fullName)
        assertEquals("152314", match?.golfCanadaCardId)
        assertEquals("Snake Point Golf Club", match?.homeCourse)
    }

    @Test
    fun `findMember returns null when member id is missing`() {
        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "Ellis, Dean",
                memberId = null,
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertNull(match)
    }

    @Test
    fun `findMember caches repeated lookups for same parsed key`() {
        `when`(membersApiFactory.create()).thenReturn(membersApi)
        `when`(membersApi.getProfile(152314L)).thenReturn(Profile().homeCourse("Snake Point Golf Club"))
        val parsed = ParsedPlayerHistory(
            playerName = "Ellis, Dean",
            memberId = "152314",
            homeCourse = "Snake Point",
            rounds = emptyList()
        )

        service.findMember(parsed)
        service.findMember(parsed)

        verify(membersApi, times(1)).getProfile(152314L)
    }

    @Test
    fun `findMember throws verification exception when profile lookup fails`() {
        `when`(membersApiFactory.create()).thenReturn(membersApi)
        `when`(membersApi.getProfile(152314L)).thenThrow(RuntimeException("boom"))

        assertThrows(VerificationProcessingException::class.java) {
            service.findMember(
                ParsedPlayerHistory(
                    playerName = "Ellis, Dean",
                    memberId = "152314",
                    homeCourse = "Snake Point",
                    rounds = emptyList()
                )
            )
        }
    }

    @Test
    fun `findMember returns null when parsed and profile home course do not match`() {
        `when`(membersApiFactory.create()).thenReturn(membersApi)
        `when`(membersApi.getProfile(152314L)).thenReturn(Profile().homeCourse("Different Course"))

        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "Ellis, Dean",
                memberId = "152314",
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertNull(match)
    }
}
