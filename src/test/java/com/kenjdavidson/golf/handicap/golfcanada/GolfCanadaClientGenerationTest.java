package com.kenjdavidson.golf.handicap.golfcanada;

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi;
import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi;
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient;
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryEntry;
import com.kenjdavidson.golf.handicap.golfcanada.model.HistoryResponse;
import com.kenjdavidson.golf.handicap.golfcanada.model.Profile;
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
        assertNotNull(HistoryResponse.class.getMethod("getData"));
        assertNotNull(HistoryEntry.class.getMethod("getCourse"));
    }
}
