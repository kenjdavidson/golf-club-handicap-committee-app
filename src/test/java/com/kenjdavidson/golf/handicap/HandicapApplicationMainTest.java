package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandicapApplicationMainTest {

    @Test
    void mainStartsSpringAndLaunchesDesktopApp() {
        String[] args = new String[]{"--spring.profiles.active=test"};
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("server.ssl.key-store")).thenReturn(null);
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");

        try (MockedStatic<GolfCanadaSslTrustConfigurator> sslConfigurator = mockStatic(GolfCanadaSslTrustConfigurator.class);
             MockedStatic<DesktopAppLauncher> desktopLauncher = mockStatic(DesktopAppLauncher.class);
             MockedConstruction<SpringApplicationBuilder> springBuilders = mockConstruction(SpringApplicationBuilder.class,
                     (mock, invocation) -> {
                         when(mock.headless(false)).thenReturn(mock);
                         when(mock.run(any(String[].class))).thenReturn(context);
                     })) {

            HandicapApplication.main(args);

            SpringApplicationBuilder builder = springBuilders.constructed().getFirst();
            verify(builder).headless(false);
            verify(builder).run(eq(args));
            sslConfigurator.verify(GolfCanadaSslTrustConfigurator::configureDefaultSslTrust);
            desktopLauncher.verify(() -> DesktopAppLauncher.launchApp(args));
        }
    }
}
