package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.AppointmentsCounts;
import uk.gov.companieshouse.company.metrics.repository.metrics.AppointmentRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AppointmentsCountService {

    private final Logger logger;
    private final AppointmentRepository appointmentsRepository;

    /**
     * Constructor.
     */
    public AppointmentsCountService(Logger logger, AppointmentRepository appointmentsRepository) {
        this.logger = logger;
        this.appointmentsRepository = appointmentsRepository;
    }

    /**
     * Save or Update company_metrics for appointments.
     *
     * @param contextId     Request context ID
     * @param companyNumber The ID of the company to update metrics for
     * @param metricsApi    to update
     */
    public void recalculateMetrics(String contextId, String companyNumber, MetricsApi metricsApi) {

        logger.debug(String.format("Recalculating appointments metrics for %s with context-id %s",
                companyNumber, contextId));

        AppointmentsCounts appointmentsCounts = appointmentsRepository.getCounts(companyNumber);

        AppointmentsApi appointmentsApi = new AppointmentsApi();
        appointmentsApi.setTotalCount(appointmentsCounts.getTotalCount());
        appointmentsApi.setActiveCount(appointmentsCounts.getActiveCount());
        appointmentsApi.setActiveDirectorsCount(appointmentsCounts.getActiveDirectorsCount());
        appointmentsApi.setActiveSecretariesCount(appointmentsCounts.getActiveSecretariesCount());
        appointmentsApi.setActiveLlpMembersCount(appointmentsCounts.getActiveLlpMembersCount());
        appointmentsApi.setResignedCount(appointmentsCounts.getResignedCount());

        CountsApi countsApi = Optional.ofNullable(metricsApi.getCounts())
                .orElseGet(() -> {
                    CountsApi counts = new CountsApi();
                    metricsApi.setCounts(counts);
                    return counts;
                });
        countsApi.setAppointments(appointmentsApi);
    }
}
