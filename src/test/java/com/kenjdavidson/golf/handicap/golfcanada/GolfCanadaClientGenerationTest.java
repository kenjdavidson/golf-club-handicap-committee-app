package com.kenjdavidson.golf.handicap.golfcanada;

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi;
import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi;
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryResponse;
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile;
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClub;
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClubDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GolfCanadaClientGenerationTest {

    @Test
    void generatedClientContainsRequiredOperations() throws NoSuchMethodException {
        ApiClient apiClient = new ApiClient();
        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        MembersApi membersApi = new MembersApi(apiClient);

        Method authenticateMethod = authenticationApi.getClass().getMethod(
            "authenticate",
            String.class, String.class, String.class, Boolean.class, String.class, String.class, String.class
        );
        Method getProfileMethod = membersApi.getClass().getMethod("getProfile", Long.class);
        Method getHistoryMethod = membersApi.getClass().getMethod("getHistory", Long.class, Integer.class, Integer.class);

        assertNotNull(authenticateMethod);
        assertNotNull(getProfileMethod);
        assertNotNull(getHistoryMethod);
        assertEquals(AuthToken.class, authenticateMethod.getReturnType());
        assertEquals(Profile.class, getProfileMethod.getReturnType());
        assertEquals(HistoryResponse.class, getHistoryMethod.getReturnType());

        assertNotNull(Profile.class.getMethod("getClub"));
        assertNotNull(Profile.class.getMethod("getHandicap"));
        assertNotNull(Profile.class.getMethod("getLevel"));
        assertNotNull(Profile.class.getMethod("getCity"));
        assertNotNull(Profile.class.getMethod("getRegion"));
        assertNotNull(Profile.class.getMethod("getTotal"));
        assertNotNull(Profile.class.getMethod("getIsFriend"));
        assertNotNull(Profile.class.getMethod("getShowComplete"));
        assertNotNull(Profile.class.getMethod("getEmail"));
        assertEquals(ProfileClub.class, Profile.class.getMethod("getClub").getReturnType());
        assertNotNull(ProfileClub.class.getMethod("getName"));
        assertNotNull(ProfileClub.class.getMethod("getLine1"));
        assertNotNull(ProfileClub.class.getMethod("getLine2"));
        assertNotNull(ProfileClub.class.getMethod("getCity"));
        assertNotNull(ProfileClub.class.getMethod("getRegion"));
        assertNotNull(ProfileClub.class.getMethod("getPhone"));
        assertNotNull(ProfileClub.class.getMethod("getUrl"));
        assertNotNull(ProfileClub.class.getMethod("getLogoAtOdataMediaReadLink"));
        assertNotNull(HistoryResponse.class.getMethod("getData"));
        assertNotNull(HistoryEntry.class.getMethod("getCourse"));
    }
}
