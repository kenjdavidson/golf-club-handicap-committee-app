package com.kenjdavidson.golf.handicap;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VaadinFeatureFlagsTest {

    @Test
    void enablesModularUploadExperimentalFeature() throws Exception {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("vaadin-featureflags.properties")) {
            assertThat(inputStream).isNotNull();
            properties.load(inputStream);
        }

        assertThat(properties.getProperty("com.vaadin.experimental.modularUpload")).isEqualTo("true");
    }
}
