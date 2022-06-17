package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
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
     * @param satisfiedCount   satisfied_count.
     * @param partSatisfiedCount   part_satisfied_count.
     * @param companyMetricsDocument   CompanyMetricsDocument.
     */
    public void upsertMetrics(String contextId, Integer totalCount,
                              Integer satisfiedCount,
                              Integer partSatisfiedCount, String updatedBy,
                              CompanyMetricsDocument companyMetricsDocument) {
        logger.debug(String.format("Started : Save or Update Company_Metrics with totalCount %s "
                        + "and satisfiedCount %s and  partSatisfiedCount %s ",
                         totalCount, satisfiedCount, partSatisfiedCount));

        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();
        companyMetricsDocument.setUpdated(populateUpdated(updatedBy));
        metricsApi.setEtag(GenerateEtagUtil.generateEtag());

        if (metricsApi.getMortgage() != null) {
            metricsApi.getMortgage().setTotalCount(totalCount);
            metricsApi.getMortgage().setSatisfiedCount(satisfiedCount);
            metricsApi.getMortgage().setPartSatisfiedCount(partSatisfiedCount);
        } else {
            metricsApi.setMortgage(populateMortgageApi(
                    totalCount, satisfiedCount,partSatisfiedCount));

        }

        logger.debug("Started : Saving charges in DB ");
        CompanyMetricsDocument savedCompanyMetrics =
                companyMetricsRepository.save(companyMetricsDocument);
        logger.info(String.format("Company metrics  updated for context id %s "
                        + "with id %s",
                contextId, savedCompanyMetrics.getId()));

    }

    /**
     * Add or Insert company_metrics.
     *
     * @param id id.
     * @param totalCount total_count.
     * @param satisfiedCount      satisfied_count.
     * @param partSatisfiedCount   part_satisfied_count.
     * @param updatedBy   updatedBy.
     */
    public void insertMetrics(String contextId, String id,
                              Integer totalCount, Integer satisfiedCount,
                              Integer partSatisfiedCount, String updatedBy) {

        logger.info("companyMetricsDocument with id not found hence creating "
                + "a new record in the company_metrics collection with id: %s " + id);

        var companyMetricsDocument =
                populateCompanyMetrics(id,totalCount,satisfiedCount,partSatisfiedCount,updatedBy);

        logger.debug("Started : inserting a new record in metrics collection ");
        companyMetricsRepository.save(companyMetricsDocument);
        logger.info(String.format("Company metrics added for context id %s "
                        + "with id %s",
                contextId, id));
    }


    /**
     *  Query company metrics collection.
     *
     *  @param companyNumber companyNumber
     *  @param status status
     *  @return Integer
     */
    public Integer queryCompanyMortgages(String companyNumber, String status) {
        return status.equalsIgnoreCase("none")
                ? chargesRepository.getTotalCharges(companyNumber) :
                chargesRepository.getPartSatisfiedCharges(companyNumber, status);

    }

    /**
     *  Query company metrics collection.
     *
     *  @param companyNumber companyNumber
     *  @param satisfied satisfied
     *  @param fullySatisfied fullySatisfied
     *  @return Integer
     */
    public Integer queryCompanySatisfiedMortgages(String companyNumber,
                                                  String satisfied,
                                                  String fullySatisfied) {
        return chargesRepository.getSatisfiedAndFullSatisfiedCharges(companyNumber,
                satisfied, fullySatisfied);
    }

    private CompanyMetricsDocument populateCompanyMetrics(String id,
                             Integer totalCount, Integer satisfiedCount,
                             Integer partSatisfiedCount, String updatedBy) {
        var companyMetricsDocument  = new CompanyMetricsDocument();
        companyMetricsDocument.setId(id);
        var metricsApi = new MetricsApi();
        metricsApi.setEtag(GenerateEtagUtil.generateEtag());
        var mortgageApi = populateMortgageApi(
                totalCount,satisfiedCount,partSatisfiedCount);
        metricsApi.setMortgage(mortgageApi);

        companyMetricsDocument.setUpdated(populateUpdated(updatedBy));
        companyMetricsDocument.setCompanyMetrics(metricsApi);

        return companyMetricsDocument;
    }

    private Updated populateUpdated(String updatedBy) {

        Updated updated = new Updated();
        updated.setBy(updatedBy);
        updated.setAt(LocalDateTime.now());
        updated.setType("company_metrics");
        return updated;
    }

    private MortgageApi populateMortgageApi(Integer totalCount, Integer satisfiedCount,
                                             Integer partSatisfiedCount) {
        var mortgageApi = new MortgageApi();

        mortgageApi.setTotalCount(totalCount);
        mortgageApi.setSatisfiedCount(satisfiedCount);
        mortgageApi.setPartSatisfiedCount(partSatisfiedCount);

        return mortgageApi;
    }

}
