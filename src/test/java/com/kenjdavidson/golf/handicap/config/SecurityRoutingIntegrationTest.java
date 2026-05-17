package com.kenjdavidson.golf.handicap.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityRoutingIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    @LocalServerPort
    private int port;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void anonymousUsersAreRedirectedToLogin() throws IOException, InterruptedException {
        HttpResponse<Void> response = HTTP_CLIENT.send(request("/"), HttpResponse.BodyHandlers.discarding());

        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("location").orElse("").contains("/login"));
    }

    @Test
    void loginPageRemainsPublic() throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP_CLIENT.send(request("/login"), HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("content-type").orElse("").contains("text/html"));
    }

    @Test
    void configuredCommitteeUserIsAvailable() {
        UserDetails user = userDetailsService.loadUserByUsername("committee");

        assertEquals("committee", user.getUsername());
        assertTrue(user.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_COMMITTEE".equals(authority.getAuthority())));
    }

    private HttpRequest request(String path) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + path))
            .GET()
            .build();
    }
}
