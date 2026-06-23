package com.kenjdavidson.golf.handicap.golfcanada;

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi;
import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi;
import com.kenjdavidson.golf.handicap.golfcanada.api.ScoresApi;
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryResponse;
import com.kenjdavidson.golf.handicap.golfcanada.model.HoleScore;
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile;
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClub;
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClubDeserializer;
import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails;
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
        ScoresApi scoresApi = new ScoresApi(apiClient);

        Method authenticateMethod = authenticationApi.getClass().getMethod(
            "authenticate",
            String.class, String.class, String.class, Boolean.class, String.class, String.class, String.class
        );
        Method getProfileMethod = membersApi.getClass().getMethod("getProfile", Long.class);
        Method getHistoryMethod = membersApi.getClass().getMethod("getHistory", Long.class, Integer.class, Integer.class);
        Method getScoreDetailsMethod = scoresApi.getClass().getMethod("getScoreDetails", Long.class);

        assertNotNull(authenticateMethod);
        assertNotNull(getProfileMethod);
        assertNotNull(getHistoryMethod);
        assertNotNull(getScoreDetailsMethod);
        assertEquals(AuthToken.class, authenticateMethod.getReturnType());
        assertEquals(Profile.class, getProfileMethod.getReturnType());
        assertEquals(HistoryResponse.class, getHistoryMethod.getReturnType());
        assertEquals(ScoreDetails.class, getScoreDetailsMethod.getReturnType());

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

        assertNotNull(ScoreDetails.class.getMethod("getId"));
        assertNotNull(ScoreDetails.class.getMethod("getCourse"));
        assertNotNull(ScoreDetails.class.getMethod("getHoleScores"));
        assertNotNull(ScoreDetails.class.getMethod("getDifferential"));
        assertNotNull(HoleScore.class.getMethod("getHoleNumber"));
        assertNotNull(HoleScore.class.getMethod("getGross"));
        assertNotNull(HoleScore.class.getMethod("getPar"));
        assertNotNull(HoleScore.class.getMethod("getLabel"));
        assertNotNull(HoleScore.class.getMethod("getEsc"));
    }
}
