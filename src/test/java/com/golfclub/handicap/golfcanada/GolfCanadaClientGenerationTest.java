package com.golfclub.handicap.golfcanada;

import com.golfclub.handicap.golfcanada.api.AuthenticationApi;
import com.golfclub.handicap.golfcanada.api.MembersApi;
import com.golfclub.handicap.golfcanada.invoker.ApiClient;
import com.golfclub.handicap.golfcanada.model.AuthToken;
import com.golfclub.handicap.golfcanada.model.HistoryEntry;
import com.golfclub.handicap.golfcanada.model.Profile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

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
        Method getHistoryMethod = membersApi.getClass().getMethod("getHistory", Long.class, Integer.class);

        assertNotNull(authenticateMethod);
        assertNotNull(getProfileMethod);
        assertNotNull(getHistoryMethod);
        assertEquals(AuthToken.class, authenticateMethod.getReturnType());
        assertEquals(Profile.class, getProfileMethod.getReturnType());
        assertEquals(List.class, getHistoryMethod.getReturnType());

        assertNotNull(Profile.class.getMethod("getClub"));
        assertNotNull(HistoryEntry.class.getMethod("getHandicap"));
    }
}
