package uk.gov.companieshouse.company.metrics.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.environment.EnvironmentReader;

class ApplicationConfigTest {

    ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
    }

    @Test
    void environmentReader() {

        EnvironmentReader environmentReader = applicationConfig.environmentReader();
        assertThat(environmentReader).isNotNull();
        assertThat(environmentReader).isInstanceOf(EnvironmentReader.class);
    }

}