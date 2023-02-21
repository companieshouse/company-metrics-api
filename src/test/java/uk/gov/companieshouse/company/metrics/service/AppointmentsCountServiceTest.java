package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.AppointmentsCounts;
import uk.gov.companieshouse.company.metrics.repository.metrics.AppointmentRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AppointmentsCountServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";
    private static final String E_TAG = "etag";

    final AppointmentsCounts counts = new AppointmentsCounts()
            .setActiveCount(3)
            .setTotalCount(5)
            .setResignedCount(2)
            .setActiveDirectorsCount(1)
            .setActiveSecretariesCount(1)
            .setActiveLlpMembersCount(1);

    @Mock
    Logger logger;

    @Mock
    AppointmentRepository appointmentsRepository;

    @InjectMocks
    AppointmentsCountService appointmentsCountService;

    @Test
    void shouldRecalculateMetricsWhenCountsApiElementExists() {
        MetricsApi metricsApi = new MetricsApi();
        metricsApi.setEtag(E_TAG);
        metricsApi.setCounts(new CountsApi());
        metricsApi.getCounts().setAppointments(new AppointmentsApi());

        when(appointmentsRepository.getCounts(COMPANY_NUMBER)).thenReturn(counts);

        appointmentsCountService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER, metricsApi);

        AppointmentsApi results = metricsApi.getCounts().getAppointments();
        assertThat(results).isNotNull();
        assertThat(results.getActiveCount()).isEqualTo(3);
        assertThat(results.getTotalCount()).isEqualTo(5);
        assertThat(results.getResignedCount()).isEqualTo(2);
        assertThat(results.getActiveDirectorsCount()).isEqualTo(1);
        assertThat(results.getActiveSecretariesCount()).isEqualTo(1);
        assertThat(results.getActiveLlpMembersCount()).isEqualTo(1);
    }

    @Test
    void shouldRecalculateMetricsWhenCountsApiElementDoesNotExists() {
        MetricsApi metricsApi = new MetricsApi();
        metricsApi.setEtag(E_TAG);

        when(appointmentsRepository.getCounts(COMPANY_NUMBER)).thenReturn(counts);

        appointmentsCountService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER, metricsApi);

        AppointmentsApi results = metricsApi.getCounts().getAppointments();
        assertThat(results).isNotNull();
        assertThat(results.getActiveCount()).isEqualTo(3);
        assertThat(results.getTotalCount()).isEqualTo(5);
        assertThat(results.getResignedCount()).isEqualTo(2);
        assertThat(results.getActiveDirectorsCount()).isEqualTo(1);
        assertThat(results.getActiveSecretariesCount()).isEqualTo(1);
        assertThat(results.getActiveLlpMembersCount()).isEqualTo(1);
    }
}