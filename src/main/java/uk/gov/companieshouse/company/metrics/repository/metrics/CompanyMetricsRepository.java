package uk.gov.companieshouse.company.metrics.repository.metrics;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;

@Repository
public interface CompanyMetricsRepository extends MongoRepository<CompanyMetricsDocument, String> {

}
