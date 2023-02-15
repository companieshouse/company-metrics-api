package uk.gov.companieshouse.company.metrics.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    public static final String CONTEXT_ID = "12345";
    public static final String UPDATED_BY = "updatedBy";
    public static final String COMPANY_METRICS = "company_metrics";

    @Mock
    CompanyMetricsRepository companyMetricsRepository;

    @Mock
    ChargesCountService chargesCountService;

    @Mock
    Logger logger;

    ArgumentCaptor<CompanyMetricsDocument> companyMetricsDocumentCaptor = ArgumentCaptor.forClass(CompanyMetricsDocument.class);

    @InjectMocks
    CompanyMetricsService companyMetricsService;

    private final TestData testData = new TestData();

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
                companyMetricsService.get(COMPANY_NUMBER);

        assertThat(companyMetricsActual.get()).isSameAs(companyMetricsDocument);
        verify(companyMetricsRepository).findById(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("When no company metrics is retrieved then return empty optional")
    void getNoCompanyMetricsReturned() {
        when(companyMetricsRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        Optional<CompanyMetricsDocument> companyMetricsActual =
                companyMetricsService.get(COMPANY_NUMBER);

        assertTrue(companyMetricsActual.isEmpty());
        verify(companyMetricsRepository).findById(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Should update existing charges metrics")
    void shouldUpdateExistingChargesMetrics() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateCompanyMetricsDocument();
        String initialETTag = companyMetricsDocument.getCompanyMetrics().getEtag();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        verify(chargesCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                companyMetricsDocument.getCompanyMetrics());

        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }

    @Test
    @DisplayName("Should set charges metrics on a new document")
    void shouldSetChargesMetricsOnNewDocument() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        verify(chargesCountService).recalculateMetrics(eq(CONTEXT_ID), eq(COMPANY_NUMBER),
                any());

        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should Fail with Bad Request (400) when no count type is set")
    void shouldFailWithBadRequestWhenNoCountTypeIsSet() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        assertThatThrownBy(() ->companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateEmptyMetricsRecalculateApi()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(chargesCountService, never()).recalculateMetrics(any(), any(), any());
        verify(companyMetricsRepository, never()).save(any());
    }
}