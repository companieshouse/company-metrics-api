package uk.gov.companieshouse.company.metrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.config.CucumberSpringConfiguration;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CompanyMetricsControllerITest extends CucumberSpringConfiguration {
    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String COMPANY_URL = String.format("/company/%s/metrics", MOCK_COMPANY_NUMBER);

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyMetricsService companyMetricsService;

    @Autowired
    private TestRestTemplate restTemplate;

    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();
    }

    @Test
    @DisplayName("Retrieve company metrics for a given company number")
    void getCompanyMetrics() throws Exception {

        MetricsApi metricsApi = testData.
                createMetricsApi("source-metrics-body-1.json");

        Updated updated = testData.
                createUpdated("source-metrics-updated-body-1.json");

        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);

        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.of(companyMetricsDocument));

        ResponseEntity<MetricsApi> responseEntity =
                restTemplate.getForEntity(COMPANY_URL, MetricsApi.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).usingRecursiveComparison()
                .isEqualTo(companyMetricsDocument.getCompanyMetrics());
    }

    @Test
    @DisplayName(
            "Given a company number with no matching company metrics return a not found response")
    void getCompanyMetricsNotFound() throws Exception {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.empty());

        ResponseEntity<MetricsApi> responseEntity =
                restTemplate.getForEntity(COMPANY_URL, MetricsApi.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test()
    @DisplayName("When calling get company metrics - returns a 500 INTERNAL SERVER ERROR")
    void getCompanyMetricsInternalServerError() throws Exception {
        when(companyMetricsService.get(any())).thenThrow(RuntimeException.class);

        ResponseEntity<MetricsApi> responseEntity =
                restTemplate.getForEntity(COMPANY_URL, MetricsApi.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}