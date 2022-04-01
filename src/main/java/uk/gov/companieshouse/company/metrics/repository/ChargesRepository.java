package uk.gov.companieshouse.company.metrics.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;

@Repository
public interface ChargesRepository extends MongoRepository<ChargesDocument, String> {

    @Query(value = "{company_number: ?0}",count = true)
    Integer getTotalCharges(String companyNumber);

    @Query(value = "{company_number: ?0, status: ?1}",count = true)
    Integer getPartOrFullSatisfiedCharges(String companyNumber, String status);

}
