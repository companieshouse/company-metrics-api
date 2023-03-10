package uk.gov.companieshouse.company.metrics.repository.pscs;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.PscStatementDocument;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;

@Repository
public interface PscStatementsRepository extends MongoRepository<PscStatementDocument, String> {
    @Aggregation(pipeline = {""
            + "{ $match: { 'company_number': ?0 } }",
            "{"
            + "    $facet: {"
            + "        'total': ["
            + "            { $count: 'count' }"
            + "        ]"
            + "    }"
            + "}",
            "{ $unwind: { path: '$total', preserveNullAndEmptyArrays: true } }",
            "{"
            + "    $project: {"
            + "        'statements_count': { $ifNull: ['$total.count', NumberInt(0)] }"
            + "    }"
            + "}"})
    PscStatementsCounts getCounts(String companyNumber);
}
