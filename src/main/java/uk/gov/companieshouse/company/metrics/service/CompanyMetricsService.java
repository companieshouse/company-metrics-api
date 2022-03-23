package uk.gov.companieshouse.company.metrics.service;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.repository.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyMetricsService {

    private final Logger logger;
    private final CompanyMetricsRepository companyMetricsRepository;

    /**
     * Constructor.
     */
    public CompanyMetricsService(Logger logger,
                                 CompanyMetricsRepository companyMetricsRepository) {
        this.logger = logger;
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
}
