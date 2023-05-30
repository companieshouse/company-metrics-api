package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.company.metrics.model.AppointmentsCounts;
import uk.gov.companieshouse.company.metrics.repository.appointments.AppointmentRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class AppointmentsCountServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";

    private final AppointmentsCounts counts = new AppointmentsCounts()
            .setActiveCount(3)
            .setTotalCount(5)
            .setResignedCount(2)
            .setActiveDirectorsCount(1)
            .setActiveSecretariesCount(1)
            .setActiveLlpMembersCount(1);
    private final AppointmentsCounts noAppointmentsCounts = new AppointmentsCounts()
            .setActiveCount(0)
            .setTotalCount(0)
            .setResignedCount(0)
            .setActiveDirectorsCount(0)
            .setActiveSecretariesCount(0)
            .setActiveLlpMembersCount(0);

    @Mock
    private Logger logger;

    @Mock
    private AppointmentRepository appointmentsRepository;

    @InjectMocks
    private AppointmentsCountService appointmentsCountService;

    @Test
    void shouldRecalculateAppointmentsMetrics() {

        when(appointmentsRepository.getCounts(COMPANY_NUMBER)).thenReturn(counts);

        AppointmentsApi appointments = appointmentsCountService.recalculateMetrics(CONTEXT_ID,
                COMPANY_NUMBER);

        assertThat(appointments.getActiveCount()).isEqualTo(3);
        assertThat(appointments.getTotalCount()).isEqualTo(5);
        assertThat(appointments.getResignedCount()).isEqualTo(2);
        assertThat(appointments.getActiveDirectorsCount()).isEqualTo(1);
        assertThat(appointments.getActiveSecretariesCount()).isEqualTo(1);
        assertThat(appointments.getActiveLlpMembersCount()).isEqualTo(1);
    }

    @Test
    void shouldRemoveAppointmentsMetricsWhenAppointmentsCountsAreZero() {
        when(appointmentsRepository.getCounts(COMPANY_NUMBER)).thenReturn(noAppointmentsCounts);

        AppointmentsApi appointments = appointmentsCountService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);

        assertThat(appointments.getActiveCount()).isZero();
        assertThat(appointments.getTotalCount()).isZero();
        assertThat(appointments.getResignedCount()).isZero();
        assertThat(appointments.getActiveDirectorsCount()).isZero();
        assertThat(appointments.getActiveSecretariesCount()).isZero();
        assertThat(appointments.getActiveLlpMembersCount()).isZero();
    }
}