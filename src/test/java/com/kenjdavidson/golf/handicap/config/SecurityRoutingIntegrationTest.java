package com.kenjdavidson.golf.handicap.config;

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken;
import com.kenjdavidson.golf.handicap.golfcanada.model.User;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser;
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityRoutingIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    @LocalServerPort
    private int port;

    @MockitoBean
    private GolfCanadaAuthenticationService golfCanadaAuthenticationService;

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
    void successfulGolfCanadaLoginRedirectsToRoot() throws IOException, InterruptedException {
        when(golfCanadaAuthenticationService.authenticate("golf.user@example.com", "test-password"))
            .thenReturn(new GolfCanadaAuthenticatedUser(
                new AuthToken().accessToken("access-token").user(new User()
                    .username("golf.user@example.com")
                    .fullName("Golf User")
                    .email("golf.user@example.com")
                    .golfCanadaCardId("1234567")
                    .handicap("8.4")),
                "golf.user@example.com",
                "Golf User",
                "golf.user@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            ));

        HttpResponse<Void> response = HTTP_CLIENT.send(loginRequest(), HttpResponse.BodyHandlers.discarding());

        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("location").orElse("").endsWith("/"));
        verify(golfCanadaAuthenticationService).authenticate("golf.user@example.com", "test-password");
    }

    private HttpRequest request(String path) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + path))
            .GET()
            .build();
    }

    private HttpRequest loginRequest() {
        String body = "username=" + URLEncoder.encode("golf.user@example.com", StandardCharsets.UTF_8)
            + "&password=" + URLEncoder.encode("test-password", StandardCharsets.UTF_8);

        return HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + "/login"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    }
}
