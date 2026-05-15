package com.golfclub.handicap.golfcanada;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GolfCanadaAuthTokenIntegrationTest {

    private static final String SCOPE = "address+email+offline_access+openid+phone+profile+roles";

    @Test
    void authenticateWithEnvironmentCredentials() {
        String username = System.getenv("GOLFCANADA_USERNAME");
        String password = System.getenv("GOLFCANADA_PASSWORD");

        Assumptions.assumeTrue(username != null && !username.isBlank());
        Assumptions.assumeTrue(password != null && !password.isBlank());

        String body = "grant_type=password"
            + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
            + "&scope=" + SCOPE
            + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "https://scg.golfcanada.ca/connect/token",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class
        );

        HttpStatusCode statusCode = response.getStatusCode();
        assertTrue(statusCode.is2xxSuccessful(), "Expected successful auth response");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"access_token\""));
    }
}
