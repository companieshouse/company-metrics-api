package uk.gov.companieshouse.company.metrics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsServiceTest {
    private static final String MOCK_COMPANY_NUMBER = "12345678";

    @Mock
    CompanyMetricsRepository companyMetricsRepository;

    @Mock
    Logger logger;

    @InjectMocks
    CompanyMetricsService companyMetricsService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();
    }

    @Test
    @DisplayName("When company metrics is retrieved successfully then it is returned")
    void getCompanyMetrics() throws IOException {

        MetricsApi metricsApi = testData.
                createMetricsApi("source-metrics-body-1.json");

        Updated updated = testData.
                createUpdated("source-metrics-updated-body-1.json");

        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);

        when(companyMetricsRepository.findById(anyString()))
                .thenReturn(Optional.of(companyMetricsDocument));

        Optional<CompanyMetricsDocument> companyMetricsActual =
                companyMetricsService.get(MOCK_COMPANY_NUMBER);

        assertThat(companyMetricsActual.get()).isSameAs(companyMetricsDocument);
        verify(logger, times(2)).trace(anyString());
    }

    @Test
    @DisplayName("When no company metrics is retrieved then return empty optional")
    void getNoCompanyMetricsReturned() {
        when(companyMetricsRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        Optional<CompanyMetricsDocument> companyMetricsActual =
                companyMetricsService.get(MOCK_COMPANY_NUMBER);

        assertTrue(companyMetricsActual.isEmpty());
        verify(logger, times(2)).trace(anyString());
    }
}