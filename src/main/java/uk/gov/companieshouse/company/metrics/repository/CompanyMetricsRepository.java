package uk.gov.companieshouse.company.metrics.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;

public interface CompanyMetricsRepository extends MongoRepository<CompanyMetricsDocument, String> {
}
