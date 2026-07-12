package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandicapApplicationMainTest {

    @Test
    void mainActivatesDesktopProfileOnWindows() {
        runWithOs("Windows 10", args -> {
            verify(args.builder).properties("spring.profiles.default=desktop");
            verify(args.builder).run(eq(args.cliArgs));
        });
    }

    @Test
    void mainActivatesDesktopProfileOnMac() {
        runWithOs("Mac OS X", args -> {
            verify(args.builder).properties("spring.profiles.default=desktop");
            verify(args.builder).run(eq(args.cliArgs));
        });
    }

    @Test
    void mainDoesNotActivateDesktopProfileOnLinux() {
        runWithOs("Linux", args -> {
            verify(args.builder, never()).properties(any(String.class));
            verify(args.builder).run(eq(args.cliArgs));
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private record TestArgs(SpringApplicationBuilder builder, String[] cliArgs) {}

    private void runWithOs(String osName, java.util.function.Consumer<TestArgs> assertions) {
        String originalOs = System.getProperty("os.name");
        System.setProperty("os.name", osName);
        try {
            String[] args = new String[]{"--spring.profiles.active=test"};
            ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

            try (MockedStatic<GolfCanadaSslTrustConfigurator> sslConfigurator = mockStatic(GolfCanadaSslTrustConfigurator.class);
                 MockedConstruction<SpringApplicationBuilder> springBuilders = mockConstruction(SpringApplicationBuilder.class,
                         (mock, invocation) -> {
                             when(mock.headless(false)).thenReturn(mock);
                             when(mock.properties(any(String.class))).thenReturn(mock);
                             when(mock.run(any(String[].class))).thenReturn(context);
                         })) {

                HandicapApplication.main(args);

                SpringApplicationBuilder builder = springBuilders.constructed().getFirst();
                verify(builder).headless(false);
                sslConfigurator.verify(GolfCanadaSslTrustConfigurator::configureDefaultSslTrust);
                assertions.accept(new TestArgs(builder, args));
            }
        } finally {
            if (originalOs != null) {
                System.setProperty("os.name", originalOs);
            } else {
                System.clearProperty("os.name");
            }
        }
    }
}
