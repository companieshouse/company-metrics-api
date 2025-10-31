package uk.gov.companieshouse.company.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.Logger;

class LoggingConfigTest {

    private LoggingConfig loggingConfig;

    @BeforeEach
    void setUp() {
        loggingConfig = new LoggingConfig();
    }

    @Test
    void logger() {
        Logger logger = loggingConfig.logger();
        assertThat(logger).isNotNull().isInstanceOf(Logger.class);
    }

}