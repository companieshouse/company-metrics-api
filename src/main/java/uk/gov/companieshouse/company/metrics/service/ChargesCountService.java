package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class ChargesCountService {

    private static final String SATISFIED_STATUS = "satisfied";
    private static final String FULLY_SATISFIED_STATUS = "fully-satisfied";
    private static final String PART_SATISFIED_STATUS = "part-satisfied";
    private static final String NONE = "none";

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
        Integer totalCount = countCompanyMortgages(companyNumber, NONE);
        Integer satisfiedCount = countCompanySatisfiedMortgages(
                companyNumber, SATISFIED_STATUS, FULLY_SATISFIED_STATUS);
        Integer partSatisfiedCount = countCompanyMortgages(companyNumber, PART_SATISFIED_STATUS);
        MortgageApi mortgageApi = Optional.ofNullable(metricsApi.getMortgage())
                .orElseGet(() -> {
                    MortgageApi api = new MortgageApi();
                    metricsApi.setMortgage(api);
                    return api;
                });

        mortgageApi.setTotalCount(totalCount);
        mortgageApi.setSatisfiedCount(satisfiedCount);
        mortgageApi.setPartSatisfiedCount(partSatisfiedCount);
    }

    /**
     * Query company metrics collection.
     *
     * @param companyNumber companyNumber
     * @param status        status
     * @return Integer
     */
    private Integer countCompanyMortgages(String companyNumber, String status) {
        return status.equalsIgnoreCase(NONE)
                ? chargesRepository.getTotalCharges(companyNumber) :
                chargesRepository.getPartSatisfiedCharges(companyNumber, status);
    }

    /**
     * Query company metrics collection.
     *
     * @param companyNumber  companyNumber
     * @param satisfied      satisfied
     * @param fullySatisfied fullySatisfied
     * @return Integer
     */
    private Integer countCompanySatisfiedMortgages(String companyNumber, String satisfied,
            String fullySatisfied) {
        return chargesRepository.getSatisfiedAndFullSatisfiedCharges(companyNumber,
                satisfied, fullySatisfied);
    }
}
