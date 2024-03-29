package uk.gov.companieshouse.company.metrics.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;
import uk.gov.companieshouse.company.metrics.model.PscsCounts;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscRepository;
import uk.gov.companieshouse.company.metrics.repository.pscstatements.PscStatementsRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class PscCountService {

    private final Logger logger;
    private final PscStatementsRepository pscStatementsRepository;
    private final PscRepository pscRepository;

    /**
     * Constructor.
     */
    public PscCountService(Logger logger, PscStatementsRepository pscStatementsRepository,
            PscRepository pscRepository) {
        this.logger = logger;
        this.pscStatementsRepository = pscStatementsRepository;
        this.pscRepository = pscRepository;
    }

    /**
     * Save or Update company_metrics for pscs.
     *
     * @param companyNumber The ID of the company to update metrics for
     */
    public PscApi recalculateMetrics(String companyNumber) {
        logger.debug("Recalculating PSC metrics", DataMapHolder.getLogMap());

        PscStatementsCounts statementsCounts = pscStatementsRepository.getCounts(companyNumber);

        PscsCounts pscsCounts = pscRepository.getCounts(companyNumber);

        return new PscApi()
                .statementsCount(statementsCounts.getStatementsCount())
                .activeStatementsCount(statementsCounts.getActiveStatementsCount())
                .withdrawnStatementsCount(statementsCounts.getWithdrawnStatementsCount())
                .pscsCount(pscsCounts.getPscsCount())
                .activePscsCount(pscsCounts.getActivePscsCount())
                .ceasedPscsCount(pscsCounts.getCeasedPscsCount())
                .totalCount(statementsCounts.getStatementsCount() + pscsCounts.getPscsCount());
    }
}
