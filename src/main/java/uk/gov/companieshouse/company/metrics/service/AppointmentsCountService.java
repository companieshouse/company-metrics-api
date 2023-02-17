package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AppointmentsCountService {

    private final Logger logger;

    /**
     * Constructor.
     */
    public AppointmentsCountService(Logger logger) {
        this.logger = logger;
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

        CountsApi countsApi = Optional.ofNullable(metricsApi.getCounts())
                .orElseGet(() -> {
                    CountsApi counts = new CountsApi();
                    metricsApi.setCounts(counts);
                    return counts;
                });
        AppointmentsApi appointmentsApi = Optional.ofNullable(countsApi.getAppointments())
                .orElseGet(() -> {
                    AppointmentsApi appointments = new AppointmentsApi();
                    countsApi.setAppointments(appointments);
                    return appointments;
                });

        appointmentsApi.setActiveCount(0);
        appointmentsApi.setTotalCount(0);
        appointmentsApi.setResignedCount(0);
        appointmentsApi.setActiveDirectorsCount(0);
        appointmentsApi.setActiveSecretariesCount(0);
        appointmentsApi.setActiveLlpMembersCount(0);
    }
}
