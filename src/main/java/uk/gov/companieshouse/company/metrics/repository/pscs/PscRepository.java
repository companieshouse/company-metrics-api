package uk.gov.companieshouse.company.metrics.repository.pscs;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.PscDocument;
import uk.gov.companieshouse.company.metrics.model.PscsCounts;

@Repository
public interface PscRepository extends MongoRepository<PscDocument, String> {

    @Aggregation(pipeline = {"{ $match: { 'company_number': ?0 } }",
            "{"
                    + "    $facet: {"
                    + "        'total': ["
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "        'active_pscs': ["
                    + "            { $match: { 'data.ceased_on': {$exists: false} } },"
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "        'ceased_pscs': ["
                    + "            { $match: { 'data.ceased_on': {$exists: true} } },"
                    + "            { $count: 'count' }"
                    + "        ],"
                    + "    }"
                    + "}",
            "{ $unwind: { path: '$total', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$active_pscs', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$ceased_pscs', preserveNullAndEmptyArrays: true } }",
            "{"
                    + "    $project: {"
                    + "        'pscs_count': { $ifNull: ['$total.count', NumberInt(0)] },"
                    + "        'active_pscs_count': "
                    + "{ $ifNull: ['$active_pscs.count', NumberInt(0)] },"
                    + "        'ceased_pscs_count': "
                    + "{ $ifNull: ['$ceased_pscs.count', NumberInt(0)] }"
                    + "    }"
                    + "}"})
    PscsCounts getCounts(String companyNumber);
}