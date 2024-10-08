package uk.gov.companieshouse.company.metrics.repository.registers;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.registers.RegisterApi;
import uk.gov.companieshouse.company.metrics.model.ChargesCounts;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;
import uk.gov.companieshouse.company.metrics.model.RegistersDocument;

@Repository
public interface RegistersRepository extends MongoRepository<RegistersDocument, String> {
}
