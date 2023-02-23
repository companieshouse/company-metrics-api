package uk.gov.companieshouse.company.metrics.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

class CompanyMetricsDocumentRepositoryITest extends AbstractIntegrationTest {

    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private final TestData testData = new TestData();
    @Autowired
    private CompanyMetricsRepository companyMetricsRepository;

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @AfterAll
    static void tear() {
        mongoDBContainer.stop();
    }

    @BeforeEach
    void setupForEach() {
        this.companyMetricsRepository.deleteAll();
    }

    @Test
    void shouldReturnAppointmentCountsForExistingCompanyNumber() throws IOException {
        CompanyMetricsDocument expectedDoc = buildCompanyMetricsDocument(
                "source-metrics-body-1.json");

        Optional<CompanyMetricsDocument> resultDoc = this.companyMetricsRepository.findById(
                MOCK_COMPANY_NUMBER);

        assertTrue(resultDoc.isPresent());
        assertThat(resultDoc.get()).usingRecursiveComparison()
                .isEqualTo(expectedDoc);
    }

    @Test
    void shouldReturnNoCompanyMetricsForNonExistingCompanyNumber() throws IOException {
        MetricsApi metricsApi = testData.
                createMetricsApi("source-metrics-body-1.json");

        Updated updated = testData.createUpdated("source-metrics-updated-body-1.json");
        CompanyMetricsDocument expectedDocument = new CompanyMetricsDocument(metricsApi, updated);
        expectedDocument.setId(MOCK_COMPANY_NUMBER);

        this.companyMetricsRepository.save(expectedDocument);

        assertTrue(this.companyMetricsRepository.findById("non_existing_company_number").isEmpty());
    }

    @NotNull
    private CompanyMetricsDocument buildCompanyMetricsDocument(String metricsDocumentFile)
            throws IOException {
        MetricsApi metricsApi = testData.createMetricsApi(metricsDocumentFile);

        Updated updated = testData.
                createUpdated("source-metrics-updated-body-1.json");
        CompanyMetricsDocument expectedCompanyMetricsDocument = new CompanyMetricsDocument(
                metricsApi, updated);
        expectedCompanyMetricsDocument.setId(MOCK_COMPANY_NUMBER);

        this.companyMetricsRepository.save(expectedCompanyMetricsDocument);
        return expectedCompanyMetricsDocument;
    }
}
