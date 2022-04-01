package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.repository.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;



@Service
public class CompanyMetricsService {

    private final Logger logger;
    private final CompanyMetricsRepository companyMetricsRepository;
    private final ChargesRepository chargesRepository;

    /**
     * Constructor.
     */
    public CompanyMetricsService(Logger logger,
                                 CompanyMetricsRepository companyMetricsRepository,
                                 ChargesRepository chargesRepository) {
        this.logger = logger;
        this.companyMetricsRepository = companyMetricsRepository;
        this.chargesRepository = chargesRepository;
    }

    /**
     * Retrieve company metrics using its company number.
     *
     * @param companyNumber the company number
     * @return company metrics, if one with such a company number exists, otherwise an empty
     *     optional
     */
    public Optional<CompanyMetricsDocument> get(String companyNumber) {
        logger.trace(String.format("DSND-526: GET company metrics with number %s", companyNumber));
        Optional<CompanyMetricsDocument> companyMetricsDocument = companyMetricsRepository
                .findById(companyNumber);

        companyMetricsDocument.ifPresentOrElse(
                companyMetrics -> logger.trace(
                        String.format(
                                "DSND-526: Company metrics with company number %s retrieved: %s",
                                companyNumber, companyMetrics)),
                () -> logger.trace(
                        String.format("DSND-526: Company metrics with company number %s not found",
                                companyNumber))
        );

        return companyMetricsDocument;
    }


    /**
     * Save or Update company_metrics.
     *
     * @param totalCount total_count.
     * @param satisfiedCount      satisfied_count.
     * @param partSatisfiedCount   part_satisfied_count.
     * @param companyMetricsDocument   CompanyMetricsDocument.
     */
    @Transactional
    public void upsertMetrics(Integer totalCount, Integer satisfiedCount,
                              Integer partSatisfiedCount, String updatedBy,
                              CompanyMetricsDocument companyMetricsDocument) {
        logger.debug(String.format("Started : Save or Update Company_Metrics with totalCount %s "
                        + "and satisfiedCount %s and  partSatisfiedCount %s ",
                         totalCount, satisfiedCount, partSatisfiedCount));

        if (companyMetricsDocument != null && companyMetricsDocument.getCompanyMetrics() != null) {
            if (companyMetricsDocument.getUpdated() != null) {
                companyMetricsDocument.getUpdated().setBy(updatedBy);
            }
            MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
            metricsApi.setEtag(GenerateEtagUtil.generateEtag());

            if (metricsApi.getMortgage() != null) {
                metricsApi.getMortgage().setTotalCount(totalCount);
                metricsApi.getMortgage().setSatisfiedCount(satisfiedCount);
                metricsApi.getMortgage().setPartSatisfiedCount(partSatisfiedCount);
            }

            logger.debug("Started : Saving charges in DB ");
            companyMetricsRepository.save(companyMetricsDocument);
            logger.debug(String.format("Finished : Save or Update Company_Metrics "
                            + "with totalCount %s "
                            + "and satisfiedCount %s and  partSatisfiedCount %s",
                    totalCount, satisfiedCount , partSatisfiedCount));
        } else  {
            logger.info("companyMetricsDocument is null hence not "
                    + "saving or updating the company_metrics collection ");
        }

    }

    /**
     *  Query company metrics collection.
     *
     *  @param companyNumber companyNumber
     *  @param status status
     *  @return Integer
     */
    public Integer queryCompanyMetrics(String companyNumber, String status) {
        return status.equalsIgnoreCase("none") ? chargesRepository.getTotalCharges(companyNumber) :
                chargesRepository.getPartOrFullSatisfiedCharges(companyNumber, status);

    }


}
