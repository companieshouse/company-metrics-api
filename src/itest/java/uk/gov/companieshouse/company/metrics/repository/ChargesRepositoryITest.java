package uk.gov.companieshouse.company.metrics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.api.charges.ChargeApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.ChargesCounts;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

public class ChargesRepositoryITest extends AbstractIntegrationTest {

    @Autowired
    private CompanyMetricsRepository companyMetricsRepository;

    @Autowired
    private ChargesRepository chargesRepository;

    private static final String MOCK_COMPANY_NUMBER = "12345678";

    private final TestData testData = new TestData();

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setupForEach() {
        this.companyMetricsRepository.deleteAll();
        this.chargesRepository.deleteAll();
    }

    @AfterAll
    static void tear() {
        mongoDBContainer.stop();
    }

    @Test
    void should_return_mortgages_for_existing_company_number() {

        List<ChargesDocument> documentList = new ArrayList<>();
        documentList.add(populateChargesDocument("1234", MOCK_COMPANY_NUMBER,
                ChargeApi.StatusEnum.FULLY_SATISFIED));
        documentList.add(populateChargesDocument("12345", MOCK_COMPANY_NUMBER,
                ChargeApi.StatusEnum.SATISFIED));
        documentList.add(populateChargesDocument("123456", MOCK_COMPANY_NUMBER,
                ChargeApi.StatusEnum.PART_SATISFIED));
        documentList.add(populateChargesDocument("1234567", MOCK_COMPANY_NUMBER,
                ChargeApi.StatusEnum.OUTSTANDING));

        this.chargesRepository.saveAll(documentList);

        ChargesCounts chargesCounts = chargesRepository.getCounts(MOCK_COMPANY_NUMBER);
        assertEquals(4, chargesCounts.getTotalCount());
        assertEquals(2, chargesCounts.getSatisfiedOrFullySatisfied());
        assertEquals(1, chargesCounts.getPartSatisfied());
    }

    @Test
    void should_update_company_metrics_collection_with_mortgage_details() throws IOException {
        MetricsApi metricsApi = testData.
                createMetricsApi("source-metrics-body-1.json");
        Updated updated = testData.
                createUpdated("source-metrics-updated-body-1.json");
        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi,
                updated);
        companyMetricsDocument.setId(MOCK_COMPANY_NUMBER);
        companyMetricsDocument.setUpdated(updated);

        companyMetricsRepository.save(companyMetricsDocument);

        Optional<CompanyMetricsDocument> document = companyMetricsRepository.findById(
                MOCK_COMPANY_NUMBER);
        assertEquals(MOCK_COMPANY_NUMBER, document.get().getId());
        assertEquals(51, document.get().getCompanyMetrics().getMortgage().getTotalCount());
        assertEquals(42, document.get().getCompanyMetrics().getMortgage().getSatisfiedCount());
        assertEquals(0, document.get().getCompanyMetrics().getMortgage().getPartSatisfiedCount());
    }

    private ChargesDocument populateChargesDocument(String id, String companyNumber,
            ChargeApi.StatusEnum status) {

        ChargeApi chargeApi = new ChargeApi();
        chargeApi.setStatus(status);
        chargeApi.setId(id);

        var chargesDocument =
                new ChargesDocument().setCompanyNumber(companyNumber)
                        .setId(id).setData(chargeApi)
                        .setUpdated(populateUpdateObject("updatedBy"));

        return chargesDocument;
    }

    private Updated populateUpdateObject(String updatedBy) {

        Updated updated = new Updated();
        updated.setBy(updatedBy);
        updated.setAt(LocalDateTime.now(ZoneOffset.UTC));
        updated.setType("company_metrics");
        return updated;
    }
}
