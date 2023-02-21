package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
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
     * @param contextId     Request context ID
     * @param companyNumber The ID of the company to update metrics for
     * @param metricsApi    to update
     */
    public void recalculateMetrics(String contextId, String companyNumber, MetricsApi metricsApi) {

        logger.debug(String.format("Recalculating charges metrics for %s with context-id %s",
                companyNumber, contextId));
        MortgageApi mortgageApi = Optional.ofNullable(metricsApi.getMortgage())
                .orElseGet(() -> {
                    MortgageApi api = new MortgageApi();
                    metricsApi.setMortgage(api);
                    return api;
                });

        ChargesCounts chargesCounts = chargesRepository.getCounts(companyNumber);
        mortgageApi.setTotalCount(chargesCounts.getTotalCount());
        mortgageApi.setSatisfiedCount(chargesCounts.getSatisfiedOrFullySatisfied());
        mortgageApi.setPartSatisfiedCount(chargesCounts.getPartSatisfied());
    }
}
