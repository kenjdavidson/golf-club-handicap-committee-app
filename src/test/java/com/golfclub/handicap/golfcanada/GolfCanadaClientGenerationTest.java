package com.golfclub.handicap.golfcanada;

import com.golfclub.handicap.golfcanada.api.AuthenticationApi;
import com.golfclub.handicap.golfcanada.api.MembersApi;
import com.golfclub.handicap.golfcanada.invoker.ApiClient;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GolfCanadaClientGenerationTest {

    @Test
    void generatedClientContainsRequiredOperations() {
        ApiClient apiClient = new ApiClient();
        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        MembersApi membersApi = new MembersApi(apiClient);

        assertTrue(Arrays.stream(authenticationApi.getClass().getMethods())
            .anyMatch(method -> method.getName().equals("authenticate")));
        assertTrue(Arrays.stream(membersApi.getClass().getMethods())
            .anyMatch(method -> method.getName().equals("getProfile")));
        assertTrue(Arrays.stream(membersApi.getClass().getMethods())
            .anyMatch(method -> method.getName().equals("getHistory")));
    }
}
