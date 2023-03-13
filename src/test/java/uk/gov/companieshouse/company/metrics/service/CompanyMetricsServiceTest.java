package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.*;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class CompanyMetricsServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";
    private static final String UPDATED_BY = "updatedBy";
    private static final String COMPANY_METRICS = "company_metrics";
    private static final AppointmentsApi ZERO_APPOINTMENTS = new AppointmentsApi()
            .totalCount(0)
            .activeCount(0)
            .activeDirectorsCount(0)
            .activeSecretariesCount(0)
            .activeLlpMembersCount(0)
            .resignedCount(0);
    private static final AppointmentsApi APPOINTMENTS = new AppointmentsApi()
            .totalCount(1)
            .activeCount(1)
            .activeDirectorsCount(1)
            .activeSecretariesCount(0)
            .activeLlpMembersCount(0)
            .resignedCount(0);

    public static final PscApi PSCS = new PscApi()
            .statementsCount(3)
            .activeStatementsCount(2)
            .withdrawnStatementsCount(1)
            .pscsCount(3)
            .activePscsCount(2)
            .ceasedPscsCount(1)
            .totalCount(6);

    public static final PscApi ZERO_PSCS = new PscApi()
            .statementsCount(0)
            .activeStatementsCount(0)
            .withdrawnStatementsCount(0)
            .pscsCount(0)
            .activePscsCount(0)
            .ceasedPscsCount(0)
            .totalCount(0);
    private static final MortgageApi ZERO_MORTGAGES = new MortgageApi()
            .totalCount(0)
            .satisfiedCount(0)
            .partSatisfiedCount(0);
    private static final MortgageApi MORTGAGES = new MortgageApi()
            .totalCount(1)
            .satisfiedCount(1)
            .partSatisfiedCount(0);

    @Mock
    private CompanyMetricsRepository companyMetricsRepository;

    @Mock
    private ChargesCountService chargesCountService;

    @Mock
    private AppointmentsCountService appointmentsCountService;

    @Mock
    private PscCountService pscsCountService;

    @Mock
    private Logger logger;

    private final ArgumentCaptor<CompanyMetricsDocument> companyMetricsDocumentCaptor = ArgumentCaptor.forClass(
            CompanyMetricsDocument.class);

    @InjectMocks
    private CompanyMetricsService companyMetricsService;

    private final TestData testData = new TestData();

    @Test
    @DisplayName("When company metrics is retrieved successfully then it is returned")
    void getCompanyMetrics() throws IOException {

        MetricsApi metricsApi = testData.
                createMetricsApi("source-metrics-body-1.json");

        Updated updated = testData.
                createUpdated("source-metrics-updated-body-1.json");

        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi,
                updated);

        when(companyMetricsRepository.findById(anyString()))
                .thenReturn(Optional.of(companyMetricsDocument));

        Optional<CompanyMetricsDocument> companyMetricsActual =
                companyMetricsService.get(COMPANY_NUMBER);

        assertTrue(companyMetricsActual.isPresent());
        assertThat(companyMetricsActual).containsSame(companyMetricsDocument);
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
        when(chargesCountService.recalculateMetrics(any(), any()))
                .thenReturn(MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        verify(chargesCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());

        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }

    @Test
    @DisplayName("Should not save charges metrics when no charges in DB")
    void shouldNotSaveChargesMetricsWhenNoChargesInDB() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateCompanyMetricsDocument();
        String initialETTag = companyMetricsDocument.getCompanyMetrics().getEtag();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(chargesCountService.recalculateMetrics(any(), any()))
                .thenReturn(ZERO_MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        verify(chargesCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());

        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }

    @Test
    @DisplayName("Should update existing pscs metrics")
    void shouldUpdateExistingPscsMetrics() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        String initialETTag = companyMetricsDocument.getCompanyMetrics().getEtag();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(pscsCountService.recalculateMetrics(any(), any()))
                .thenReturn(PSCS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForPscs());

        verify(pscsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());

        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }

    @Test
    @DisplayName("Should update existing appointments metrics")
    void shouldUpdateExistingAppointmentsMetrics() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        String initialETTag = companyMetricsDocument.getCompanyMetrics().getEtag();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }

    @Test
    @DisplayName("Should not save appointments metrics when no appointments in DB")
    void shouldNotSaveAppointmentsMetricsWhenNoAppointmentsInDB() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        String initialETTag = companyMetricsDocument.getCompanyMetrics().getEtag();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(ZERO_APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocument);

        Updated updated = companyMetricsDocument.getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotEqualTo(initialETTag);
    }



    @Test
    @DisplayName("Should set charges metrics on a new document")
    void shouldSetChargesMetricsOnNewDocument() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(chargesCountService.recalculateMetrics(any(), any()))
                .thenReturn(MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        verify(chargesCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getMortgage()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should set appointments metrics on a new document")
    void shouldSetAppointmentsMetricsOnNewDocument() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getCounts()).isNotNull();
        assertThat(metricsApi.getCounts().getAppointments()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should set pscs metrics on a new document")
    void shouldSetPscsMetricsOnNewDocument() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);
        when(pscsCountService.recalculateMetrics(any(), any()))
                .thenReturn(PSCS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForPscs());

        verify(pscsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getCounts()).isNotNull();
        assertThat(metricsApi.getCounts().getPersonsWithSignificantControl()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should set appointments metrics on a document without appointments counts")
    void shouldSetAppointmentsMetricsOnDocumentWithoutAppointments() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        companyMetricsDocument.getCompanyMetrics().getCounts().setAppointments(null);
        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getCounts()).isNotNull();
        assertThat(metricsApi.getCounts().getAppointments()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should set pscs metrics on a document without pscs counts")
    void shouldSetPscsMetricsOnDocumentWithoutPscs() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        companyMetricsDocument.getCompanyMetrics().getCounts().setPersonsWithSignificantControl(null);
        when(pscsCountService.recalculateMetrics(any(), any()))
                .thenReturn(PSCS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForPscs());

        verify(pscsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getCounts()).isNotNull();
        assertThat(metricsApi.getCounts().getPersonsWithSignificantControl()).isNotNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should clean-up metrics document when count field has no data")
    void shouldCleanUpMetricsDocumentWhenCountsHasNoData() throws IOException {
        CompanyMetricsDocument companyMetricsDocument = testData.populateFullCompanyMetricsDocument();
        companyMetricsDocument.getCompanyMetrics().getCounts().setAppointments(null);
        companyMetricsDocument.getCompanyMetrics().getCounts().setPersonsWithSignificantControl(null);
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(ZERO_APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).save(companyMetricsDocumentCaptor.capture());

        Updated updated = companyMetricsDocumentCaptor.getValue().getUpdated();
        assertThat(updated.getBy()).isEqualTo(UPDATED_BY);
        assertThat(updated.getType()).isEqualTo(COMPANY_METRICS);

        MetricsApi metricsApi = companyMetricsDocumentCaptor.getValue().getCompanyMetrics();
        assertThat(metricsApi.getCounts()).isNull();
        assertThat(metricsApi.getEtag()).isNotNull();
    }

    @Test
    @DisplayName("Should delete metrics document with no metrics data")
    void shouldDeleteMetricsDocumentWithNoMetricsData() {
        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument();
        doReturn(Optional.of(companyMetricsDocument))
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        when(appointmentsCountService.recalculateMetrics(any(), any()))
                .thenReturn(ZERO_APPOINTMENTS);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForAppointments());

        verify(appointmentsCountService).recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);
        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());


        verify(companyMetricsRepository).delete(companyMetricsDocumentCaptor.capture());
    }

    @Test
    @DisplayName("Should Fail with Bad Request (400) when no count type is set")
    void shouldFailWithBadRequestWhenNoCountTypeIsSet() {
        doReturn(Optional.empty())
                .when(companyMetricsRepository).findById(COMPANY_NUMBER);

        MetricsRecalculateApi request = testData.populateEmptyMetricsRecalculateApi();
        assertThatThrownBy(
                () -> companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(chargesCountService, never()).recalculateMetrics(any(), any());
        verify(appointmentsCountService, never()).recalculateMetrics(any(), any());
        verify(pscsCountService, never()).recalculateMetrics(any(), any());

        verify(companyMetricsRepository, never()).save(any());
    }
}