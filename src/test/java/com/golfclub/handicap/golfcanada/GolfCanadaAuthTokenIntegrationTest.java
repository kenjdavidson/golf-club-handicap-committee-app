package com.golfclub.handicap.golfcanada;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GolfCanadaAuthTokenIntegrationTest {

    private static final String TOKEN_ENDPOINT_URL = "https://scg.golfcanada.ca/connect/token";
    private static final String SCOPE = "address email offline_access openid phone profile roles";
    private static final RestTemplate REST_TEMPLATE = createRestTemplate();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    @Test
    void authenticateWithEnvironmentCredentials() throws Exception {
        String username = System.getenv("GOLFCANADA_USERNAME");
        String password = System.getenv("GOLFCANADA_PASSWORD");

        Assumptions.assumeTrue(username != null && !username.isBlank());
        Assumptions.assumeTrue(password != null && !password.isBlank());

        String body = "grant_type=password"
            + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
            + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
            + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = REST_TEMPLATE.exchange(
            TOKEN_ENDPOINT_URL,
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class
        );

        HttpStatusCode statusCode = response.getStatusCode();
        assertTrue(statusCode.is2xxSuccessful(), "Expected successful auth response");
        assertNotNull(response.getBody());
        JsonNode json = OBJECT_MAPPER.readTree(response.getBody());
        JsonNode accessToken = json.get("access_token");
        assertNotNull(accessToken);
        assertFalse(accessToken.asText().isBlank());
    }
}
