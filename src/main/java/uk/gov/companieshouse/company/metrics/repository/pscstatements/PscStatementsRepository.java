package uk.gov.companieshouse.company.metrics.repository.pscstatements;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.PscStatementDocument;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;

@Repository
public interface PscStatementsRepository extends MongoRepository<PscStatementDocument, String> {

    @Aggregation(pipeline = {"{ $match: { 'company_number': ?0 } }",
            "{"
                    + "    $facet: {"
                    + "        'total': ["
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "        'active_statements': ["
                    + "            { $match: { 'data.ceased_on': {$exists: false} } },"
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "        'withdrawn_statements': ["
                    + "            { $match: { 'data.ceased_on': {$exists: true} } },"
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "    }"
                    + "}",
            "{ $unwind: { path: '$total', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$active_statements', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$withdrawn_statements', preserveNullAndEmptyArrays: true } }",
            "{"
                    + "    $project: {"
                    + "        'statements_count': { $ifNull: ['$total.count', NumberInt(0)] },"
                    + "        'active_statements_count': "
                    + "{ $ifNull: ['$active_statements.count', NumberInt(0)] },"
                    + "        'withdrawn_statements_count': "
                    + "{ $ifNull: ['$withdrawn_statements.count', NumberInt(0)] }"
                    + "    }"
                    + "}"})
    PscStatementsCounts getCounts(String companyNumber);
}
