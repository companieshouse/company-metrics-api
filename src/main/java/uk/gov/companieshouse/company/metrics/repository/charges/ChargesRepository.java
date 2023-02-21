package uk.gov.companieshouse.company.metrics.repository.charges;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.ChargesCounts;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;

@Repository
public interface ChargesRepository extends MongoRepository<ChargesDocument, String> {

    @Aggregation(pipeline = {
            "{ $match: { 'company_number': ?0 } }",

            "{ $facet: {"
                    + "'total_count': ["
                    + "         { $count: 'count' }"
                    + "     ],"
                    + "'part_satisfied': ["
                    + "    { $match: { 'data.status': 'part-satisfied' } },"
                    + "    { $count: 'count' }"
                    + "],"
                    + "'satisfied_or_fully_satisfied': ["
                    + "    { $match:"
                    + "        { $or: [{ 'data.status': 'fully-satisfied' },"
                    + "                { 'data.status': 'satisfied' }]"
                    + "        }"
                    + "    },"
                    + "    { $count: 'count' }"
                    + "] } }",

            "{ $unwind: { path: '$total_count', preserveNullAndEmptyArrays: true } }",

            "{ $unwind: { path: '$part_satisfied', preserveNullAndEmptyArrays: true } }",

            "{ $unwind: { path: '$satisfied_or_fully_satisfied', "
                    + "preserveNullAndEmptyArrays: true } }",

            "{ $project: {"
                    + "'total_count': { $ifNull: ['$total_count.count', NumberInt(0)] },"
                    + "'part_satisfied': { $ifNull: ['$part_satisfied.count', NumberInt(0)] },"
                    + "'satisfied_or_fully_satisfied': { "
                    + "     $ifNull: ['$satisfied_or_fully_satisfied.count', NumberInt(0)] },"
                    + "}}"})
    ChargesCounts getCounts(String companyNumber);
}
