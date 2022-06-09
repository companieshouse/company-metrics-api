package uk.gov.companieshouse.company.metrics.repository.charges;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;

@Repository
public interface ChargesRepository extends MongoRepository<ChargesDocument, String> {

    @Query(value = "{company_number: ?0}",count = true)
    Integer getTotalCharges(String companyNumber);

    @Query(value = "{'company_number': ?0, 'data.status': ?1}",count = true)
    Integer getPartSatisfiedCharges(String companyNumber, String status);

    @Query(value = "{'company_number': ?0, $or :[{'data.status': ?1 }, "
            + "{'data.status': ?2}]}", count = true)
    Integer getSatisfiedAndFullSatisfiedCharges(String companyNumber,
                                                String satisfied, String fullySatisfied);

}
