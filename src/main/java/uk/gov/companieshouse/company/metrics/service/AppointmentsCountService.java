package uk.gov.companieshouse.company.metrics.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.company.metrics.model.AppointmentsCounts;
import uk.gov.companieshouse.company.metrics.repository.appointments.AppointmentRepository;
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
     */
    public AppointmentsApi recalculateMetrics(String contextId, String companyNumber) {

        logger.debug(String.format("Recalculating appointments metrics for %s with context-id %s",
                companyNumber, contextId));

        AppointmentsCounts appointmentsCounts = appointmentsRepository.getCounts(companyNumber);

        logger.debug("Retrieved appointments metrics from mongo");

        return new AppointmentsApi()
                .totalCount(appointmentsCounts.getTotalCount())
                .activeCount(appointmentsCounts.getActiveCount())
                .activeDirectorsCount(appointmentsCounts.getActiveDirectorsCount())
                .activeSecretariesCount(appointmentsCounts.getActiveSecretariesCount())
                .activeLlpMembersCount(appointmentsCounts.getActiveLlpMembersCount())
                .resignedCount(appointmentsCounts.getResignedCount());
    }
}
