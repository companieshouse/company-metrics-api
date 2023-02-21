package uk.gov.companieshouse.company.metrics.repository.metrics;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company.metrics.model.AppointmentDocument;
import uk.gov.companieshouse.company.metrics.model.AppointmentsCounts;

@Repository
public interface AppointmentRepository extends MongoRepository<AppointmentDocument, String> {

    @Aggregation(pipeline = {""
            + "{ $match: { 'company_number': ?0 } }",

            "{"
            + "    $facet: {"
            + "        'total': ["
            + "            { $count: 'count' }"
            + "        ],"
            + "        'active_directors': ["
            + "            {"
            + "                $match:"
            + "                    {"
            + "                        $and: ["
            + "                            { 'data.officer_role': 'director' },"
            + "                            { 'data.resigned_on': {$exists: false} }]"
            + "                    }"
            + "            },"
            + "            { $count: 'count' }"
            + "        ],"
            + "        'resigned': ["
            + "            { $match: { 'data.resigned_on': {$exists: true} } },"
            + "            { $count: 'count' }"
            + "        ],"
            + "        'active_secretaries': ["
            + "            {"
            + "                $match:"
            + "                    {"
            + "                        $and: ["
            + "                            { 'data.officer_role': 'secretary' },"
            + "                            { 'data.resigned_on': {$exists: false} }]"
            + "                    }"
            + "            },"
            + "            { $count: 'count' }"
            + "        ],"
            + "        'active_llp_members': ["
            + "            {"
            + "                $match:"
            + "                    {"
            + "                        $and: ["
            + "                            { 'data.officer_role': /llp/ },"
            + "                            { 'data.resigned_on': {$exists: false} }]"
            + "                    }"
            + "            },"
            + "            { $count: 'count' }"
            + "        ],"
            + "        'active': ["
            + "            { $match: { 'data.resigned_on': {$exists: false} } },"
            + "            { $count: 'count' }"
            + "        ]"
            + "    }"
            + "}",

            "{ $unwind: { path: '$total', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$active_directors', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$resigned', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$active_secretaries', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$active_llp_members', preserveNullAndEmptyArrays: true } },",
            "{ $unwind: { path: '$active', preserveNullAndEmptyArrays: true } }",

            "{"
            + "    $project: {"
            + "       'active_directors_count': { "
                    + "    $ifNull: ['$active_directors.count', NumberInt(0)] },"
            + "        'active_secretaries_count': { "
                    + "    $ifNull: ['$active_secretaries.count', NumberInt(0)] },"
            + "        'active_count': { $ifNull: ['$active.count', NumberInt(0)] },"
            + "        'resigned_count': { $ifNull: ['$resigned.count', NumberInt(0)] },"
            + "        'total_count': { $ifNull: ['$total.count', NumberInt(0)] },"
            + "        'active_llp_members_count': { "
                    + "    $ifNull: ['$active_llp_members.count', NumberInt(0)] }"
            + "    }"
            + "}"})
    AppointmentsCounts getCounts(String companyNumber);
}
