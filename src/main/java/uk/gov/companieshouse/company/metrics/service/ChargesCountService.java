package uk.gov.companieshouse.company.metrics.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.company.metrics.model.ChargesCounts;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class ChargesCountService {

    private final Logger logger;
    private final ChargesRepository chargesRepository;

    /**
     * Constructor.
     */
    public ChargesCountService(Logger logger, ChargesRepository chargesRepository) {
        this.logger = logger;
        this.chargesRepository = chargesRepository;
    }

    /**
     * Save or Update company_metrics for charges.
     *
     * @param companyNumber The ID of the company to update metrics for
     * @return Recalculated charges metrics
     */
    public MortgageApi recalculateMetrics(String companyNumber) {

        logger.debug("Recalculating charges metrics", DataMapHolder.getLogMap());
        ChargesCounts chargesCounts = chargesRepository.getCounts(companyNumber);
        return new MortgageApi()
                .totalCount(chargesCounts.getTotalCount())
                .satisfiedCount(chargesCounts.getSatisfiedOrFullySatisfied())
                .partSatisfiedCount(chargesCounts.getPartSatisfied());
    }
}
