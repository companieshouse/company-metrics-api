package uk.gov.companieshouse.company.metrics.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscStatementsRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class PscCountService {

    private final Logger logger;
    private final PscStatementsRepository pscStatementsRepository;

    /**
     * Constructor.
     */
    public PscCountService(Logger logger, PscStatementsRepository pscStatementsRepository) {
        this.logger = logger;
        this.pscStatementsRepository = pscStatementsRepository;
    }

    /**
     * Save or Update company_metrics for pscs.
     *
     * @param contextId     Request context ID
     * @param companyNumber The ID of the company to update metrics for
     */
    public PscApi recalculateMetrics(String contextId, String companyNumber) {

        logger.debug(String.format("Recalculating PSC metrics for %s with context-id %s",
                companyNumber, contextId));

        PscStatementsCounts statementsCounts = pscStatementsRepository.getCounts(companyNumber);

        logger.info(String.format("Found %s statements",
                statementsCounts.getStatementsCount()));

        return new PscApi()
                .statementsCount(statementsCounts.getStatementsCount())
                .activeStatementsCount(statementsCounts.getActiveStatementsCount())
                .withdrawnStatementsCount(statementsCounts.getWithdrawnStatementsCount());
    }
}
