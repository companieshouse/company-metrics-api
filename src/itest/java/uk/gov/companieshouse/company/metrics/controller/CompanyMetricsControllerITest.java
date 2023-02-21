package uk.gov.companieshouse.company.metrics.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.company.metrics.steps.ITestUtil;

class CompanyMetricsControllerITest extends AbstractIntegrationTest {
    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String COMPANY_URL = String.format("/company/%s/metrics", MOCK_COMPANY_NUMBER);

    @MockBean
    private CompanyMetricsService companyMetricsService;

    @Autowired
    private TestRestTemplate restTemplate;

    private TestData testData;

    private final ITestUtil iTestUtil = new ITestUtil();

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

        HttpEntity<?> request = new HttpEntity<>(iTestUtil.populateHttpHeaders("1234567"));
        ResponseEntity<MetricsApi> responseEntity = restTemplate.exchange(COMPANY_URL, HttpMethod.GET, request,
                MetricsApi.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).usingRecursiveComparison()
                .isEqualTo(companyMetricsDocument.getCompanyMetrics());
    }

    @Test
    @DisplayName(
            "Given a company number with no matching company metrics return a NOT FOUND response")
    void getCompanyMetricsNotFound() {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.empty());

        HttpEntity<?> request = new HttpEntity<>(iTestUtil.populateHttpHeaders("1234567"));
        ResponseEntity<MetricsApi> responseEntity = restTemplate.exchange(COMPANY_URL, HttpMethod.GET, request,
                MetricsApi.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    @DisplayName("When calling get company metrics - returns a 500 INTERNAL SERVER ERROR")
    void getCompanyMetricsInternalServerError() {
        when(companyMetricsService.get(any())).thenThrow(RuntimeException.class);
        HttpEntity<?> request = new HttpEntity<>(iTestUtil.populateHttpHeaders("1234567"));
        ResponseEntity<MetricsApi> responseEntity = restTemplate.exchange(COMPANY_URL, HttpMethod.GET, request,
                MetricsApi.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}