package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.model.MemberSearchEntry
import com.kenjdavidson.golf.handicap.golfcanada.model.MemberSearchResponse
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CachedGolfCanadaMemberLookupServiceTest {
    private val membersApi = mock(MembersApi::class.java)
    private val service = CachedGolfCanadaMemberLookupService(membersApi)

    // ── Name-search primary path ───────────────────────────────────────────────

    @Test
    fun `findMember returns match via name search when single result matches home course`() {
        val entry = MemberSearchEntry().individualId(220507L).name("Adderley, Jim").club("Blue Springs Golf Club")
        val response = MemberSearchResponse().totalCount(1).members(listOf(entry))
        `when`(membersApi.searchMembers(0, 20, "Adderley, Jim")).thenReturn(response)
        `when`(membersApi.getProfile(220507L)).thenReturn(Profile().homeCourse("Blue Springs Golf Club"))

        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "Adderley, Jim",
                memberId = "999999",
                homeCourse = "Blue Springs",
                rounds = emptyList()
            )
        )

        assertEquals(220507L, match?.individualId)
        assertEquals("Adderley, Jim", match?.fullName)
        assertEquals("220507", match?.golfCanadaCardId)
        assertEquals("Blue Springs Golf Club", match?.homeCourse)
    }

    @Test
    fun `findMember falls back to memberId profile lookup when name search returns no home course match`() {
        val entry = MemberSearchEntry().individualId(111L).name("Ellis, Dean").club("Other Club")
        val response = MemberSearchResponse().totalCount(1).members(listOf(entry))
        `when`(membersApi.searchMembers(0, 20, "Ellis, Dean")).thenReturn(response)
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
    fun `findMember falls back to memberId profile lookup when name search returns multiple matches`() {
        val entry1 = MemberSearchEntry().individualId(111L).name("Smith, John").club("Snake Point Golf Club")
        val entry2 = MemberSearchEntry().individualId(222L).name("Smith, John").club("Snake Point Golf Club")
        val response = MemberSearchResponse().totalCount(2).members(listOf(entry1, entry2))
        `when`(membersApi.searchMembers(0, 20, "Smith, John")).thenReturn(response)
        `when`(membersApi.getProfile(152314L)).thenReturn(Profile().homeCourse("Snake Point Golf Club"))

        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "Smith, John",
                memberId = "152314",
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertEquals(152314L, match?.individualId)
    }

    @Test
    fun `findMember returns null when name search finds no match and memberId is missing`() {
        val response = MemberSearchResponse().totalCount(0).members(emptyList())
        `when`(membersApi.searchMembers(0, 20, "Ellis, Dean")).thenReturn(response)

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
    fun `findMember returns null when both name search and memberId profile lookup find no home course match`() {
        val response = MemberSearchResponse().totalCount(0).members(emptyList())
        `when`(membersApi.searchMembers(0, 20, "Ellis, Dean")).thenReturn(response)
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

    @Test
    fun `findMember throws verification exception when name search call fails`() {
        `when`(membersApi.searchMembers(0, 20, "Ellis, Dean")).thenThrow(RuntimeException("Search failed"))

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
    fun `findMember throws verification exception when profile lookup fails during name-search path`() {
        val entry = MemberSearchEntry().individualId(220507L).name("Adderley, Jim").club("Blue Springs Golf Club")
        val response = MemberSearchResponse().totalCount(1).members(listOf(entry))
        `when`(membersApi.searchMembers(0, 20, "Adderley, Jim")).thenReturn(response)
        `when`(membersApi.getProfile(220507L)).thenThrow(RuntimeException("Profile lookup failed"))

        assertThrows(VerificationProcessingException::class.java) {
            service.findMember(
                ParsedPlayerHistory(
                    playerName = "Adderley, Jim",
                    memberId = null,
                    homeCourse = "Blue Springs",
                    rounds = emptyList()
                )
            )
        }
    }

    // ── memberId fallback path ─────────────────────────────────────────────────

    @Test
    fun `findMember returns match from memberId when player name is blank`() {
        `when`(membersApi.getProfile(152314L)).thenReturn(Profile().homeCourse("Snake Point Golf Club"))

        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = "  ",
                memberId = "152314",
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertEquals(152314L, match?.individualId)
        assertEquals("Snake Point Golf Club", match?.homeCourse)
        verify(membersApi, never()).searchMembers(anyInt(), anyInt(), anyString())
    }

    @Test
    fun `findMember returns null when player name is null and memberId is also null`() {
        val match = service.findMember(
            ParsedPlayerHistory(
                playerName = null,
                memberId = null,
                homeCourse = "Snake Point",
                rounds = emptyList()
            )
        )

        assertNull(match)
    }

    @Test
    fun `findMember throws verification exception when memberId profile lookup fails`() {
        val response = MemberSearchResponse().totalCount(0).members(emptyList())
        `when`(membersApi.searchMembers(0, 20, "Ellis, Dean")).thenReturn(response)
        `when`(membersApi.getProfile(152314L)).thenThrow(RuntimeException("Profile lookup failed"))

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

    // ── Caching ────────────────────────────────────────────────────────────────

    @Test
    fun `findMember caches repeated lookups for same parsed key`() {
        val entry = MemberSearchEntry().individualId(220507L).name("Adderley, Jim").club("Blue Springs Golf Club")
        val response = MemberSearchResponse().totalCount(1).members(listOf(entry))
        `when`(membersApi.searchMembers(0, 20, "Adderley, Jim")).thenReturn(response)
        `when`(membersApi.getProfile(220507L)).thenReturn(Profile().homeCourse("Blue Springs Golf Club"))

        val parsed = ParsedPlayerHistory(
            playerName = "Adderley, Jim",
            memberId = "999999",
            homeCourse = "Blue Springs",
            rounds = emptyList()
        )

        service.findMember(parsed)
        service.findMember(parsed)

        verify(membersApi, times(1)).searchMembers(0, 20, "Adderley, Jim")
        verify(membersApi, times(1)).getProfile(220507L)
    }
}
