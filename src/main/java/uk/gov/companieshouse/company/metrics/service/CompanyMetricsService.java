package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyMetricsService {

    private static final String COMPANY_METRICS_TYPE = "company_metrics";

    private final Logger logger;

    private final ChargesCountService chargesCountService;

    private final CompanyMetricsRepository companyMetricsRepository;

    /**
     * Constructor.
     */
    public CompanyMetricsService(Logger logger,
            ChargesCountService chargesCountService,
            CompanyMetricsRepository companyMetricsRepository) {
        this.logger = logger;
        this.chargesCountService = chargesCountService;
        this.companyMetricsRepository = companyMetricsRepository;
    }

    /**
     * Retrieve company metrics using its company number.
     *
     * @param companyNumber the company number
     * @return company metrics, if one with such a company number exists, otherwise an empty
     *     optional
     */
    public Optional<CompanyMetricsDocument> get(String companyNumber) {
        return companyMetricsRepository.findById(companyNumber);
    }

    /**
     * Save or Update company_metrics for charges.
     *
     * @param contextId Request context ID
     * @param companyNumber The ID of the company to update metrics for
     * @param metricsRecalculateApi Request data that determines which metrics to update
     */
    public void recalculateMetrics(String contextId,
            String companyNumber,
            MetricsRecalculateApi metricsRecalculateApi) {

        CompanyMetricsDocument companyMetricsDocument = get(companyNumber)
                .orElseGet(() -> getCompanyMetricsDocument(companyNumber));
        MetricsApi metricsApi = companyMetricsDocument.getCompanyMetrics();

        if (BooleanUtils.isTrue(metricsRecalculateApi.getMortgage())) {
            if (metricsApi.getMortgage() == null) {
                metricsApi.setMortgage(new MortgageApi());
            }
            chargesCountService.recalculateMetrics(contextId, companyNumber, metricsApi);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Unable to process payload with context id %s and company number %s",
                    contextId, companyNumber));
        }

        String updatedBy =  metricsRecalculateApi.getInternalData() != null
                ? metricsRecalculateApi.getInternalData().getUpdatedBy() : null;
        companyMetricsDocument.setUpdated(populateUpdated(updatedBy));

        metricsApi.setEtag(GenerateEtagUtil.generateEtag());

        companyMetricsRepository.save(companyMetricsDocument);

        logger.info(String.format("Company metrics updated for context id %s with id %s",
                contextId, companyMetricsDocument.getId()));
    }

    private CompanyMetricsDocument getCompanyMetricsDocument(String companyNumber) {

        CompanyMetricsDocument companyMetricsDocument = get(companyNumber)
                .orElseGet(() -> {
                    CompanyMetricsDocument document = new CompanyMetricsDocument();
                    document.setId(companyNumber);
                    return document;
                });
        MetricsApi metricsApi = Optional.ofNullable(companyMetricsDocument.getCompanyMetrics())
                .orElse(new MetricsApi());
        companyMetricsDocument.setCompanyMetrics(metricsApi);
        return companyMetricsDocument;
    }

    private Updated populateUpdated(String updatedBy) {

        Updated updated = new Updated();
        updated.setBy(updatedBy);
        updated.setAt(LocalDateTime.now());
        updated.setType(COMPANY_METRICS_TYPE);
        return updated;
    }
}
