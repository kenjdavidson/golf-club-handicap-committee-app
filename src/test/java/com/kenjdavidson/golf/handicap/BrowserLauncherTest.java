package com.kenjdavidson.golf.handicap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.core.env.Environment;

import java.awt.Desktop;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrowserLauncherTest {

    @Test
    void runOpensLocalBrowserWhenDesktopBrowseIsSupported() throws Exception {
        Environment environment = mock(Environment.class);
        WebServerApplicationContext webServerContext = mock(WebServerApplicationContext.class);
        WebServer webServer = mock(WebServer.class);
        ApplicationArguments args = mock(ApplicationArguments.class);
        Desktop desktop = mock(Desktop.class);
        String originalHeadless = System.getProperty("java.awt.headless");

        when(webServerContext.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(8080);
        when(environment.getProperty("server.ssl.key-store")).thenReturn(null);
        when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

        try (MockedStatic<Desktop> desktopStatic = mockStatic(Desktop.class)) {
            System.setProperty("java.awt.headless", "false");
            desktopStatic.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopStatic.when(Desktop::getDesktop).thenReturn(desktop);

            new BrowserLauncher(environment, webServerContext).run(args);

            verify(desktop).browse(new URI("http://localhost:8080"));
        } finally {
            if (originalHeadless == null) {
                System.clearProperty("java.awt.headless");
            } else {
                System.setProperty("java.awt.headless", originalHeadless);
            }
        }
    }
}
