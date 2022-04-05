package uk.gov.companieshouse.company.metrics.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
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
                companyMetricsDocument.setUpdated(populateUpdated(updatedBy));
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
            logger.info("companyMetricsDocument is null hence creating "
                    + "a new record in the company_metrics collection ");
            var companyMetricsRecord  = new CompanyMetricsDocument();

        }

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
    @Transactional
    public void insertMetrics(String id, Integer totalCount, Integer satisfiedCount,
                              Integer partSatisfiedCount, String updatedBy) {

        var companyMetricsDocument =
                populateCompanyMetrics(id,totalCount,satisfiedCount,partSatisfiedCount,updatedBy);

        logger.debug("Started : inserting a new record in metrics collection ");
        companyMetricsRepository.save(companyMetricsDocument);
        logger.debug("Finished : added a record into "
                + "Company_Metrics with id " + id);

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

    private CompanyMetricsDocument populateCompanyMetrics(String id,
                             Integer totalCount, Integer satisfiedCount,
                             Integer partSatisfiedCount, String updatedBy) {
        var companyMetricsRecord  = new CompanyMetricsDocument();
        companyMetricsRecord.setId(id);
        var metricsApi = new MetricsApi();
        metricsApi.setEtag(GenerateEtagUtil.generateEtag());
        var mortgageApi = new MortgageApi();
        mortgageApi.setTotalCount(totalCount);
        mortgageApi.setSatisfiedCount(satisfiedCount);
        mortgageApi.setPartSatisfiedCount(partSatisfiedCount);
        metricsApi.setMortgage(mortgageApi);

        companyMetricsRecord.setUpdated(populateUpdated(updatedBy));
        companyMetricsRecord.setCompanyMetrics(metricsApi);

        return companyMetricsRecord;
    }

    private Updated populateUpdated(String updatedBy) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = new Date();
        String updatedAt =  simpleDateFormat.format(date);

        Updated updated = new Updated();
        updated.setBy(updatedBy);
        updated.setAt("ISODate(\"" + updatedAt + "\")");
        updated.setType("company_metrics");
        return updated;
    }

}
