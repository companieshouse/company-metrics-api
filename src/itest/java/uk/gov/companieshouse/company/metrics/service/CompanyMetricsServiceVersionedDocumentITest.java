package uk.gov.companieshouse.company.metrics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.UnversionedCompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

class CompanyMetricsServiceVersionedDocumentITest extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";
    private static final MortgageApi MORTGAGES = new MortgageApi()
            .totalCount(1)
            .satisfiedCount(1)
            .partSatisfiedCount(0);
    private static final MortgageApi EMPTY_MORTGAGES = new MortgageApi()
            .totalCount(0)
            .satisfiedCount(0)
            .partSatisfiedCount(0);
    private final TestData testData = new TestData();

    @Autowired
    private CompanyMetricsRepository companyMetricsRepository;

    @Autowired
    private CompanyMetricsService companyMetricsService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private ChargesCountService chargesCountService;

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
    void shouldCreateInitialVersionedMetricsDocument() {
        when(chargesCountService.recalculateMetrics(any()))
                .thenReturn(MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        Optional<CompanyMetricsDocument> document = companyMetricsRepository.findById(COMPANY_NUMBER);

        assertTrue(document.isPresent());
        assertEquals(0L, document.get().getVersion());

        assertNotNull(document.get().getCompanyMetrics().getMortgage());
        assertNotNull(document.get().getCompanyMetrics().getEtag());
    }

    @Test
    void shouldUpdateExistingUnversionedDocument() throws IOException {
        UnversionedCompanyMetricsDocument initialMetricsDocument = new UnversionedCompanyMetricsDocument(testData.populateFullCompanyMetricsDocument());
        initialMetricsDocument.setId(COMPANY_NUMBER);
        initialMetricsDocument.version(null);
        mongoTemplate.save(initialMetricsDocument);

        when(chargesCountService.recalculateMetrics(any()))
                .thenReturn(MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        Optional<CompanyMetricsDocument> document = companyMetricsRepository.findById(COMPANY_NUMBER);

        assertTrue(document.isPresent());
        assertEquals(0L, document.get().getVersion());

        assertNotNull(document.get().getCompanyMetrics().getMortgage());
        assertNotNull(document.get().getCompanyMetrics().getEtag());
    }

    @Test
    void shouldUpdateExistingVersionedDocument() throws IOException {
        UnversionedCompanyMetricsDocument initialMetricsDocument = new UnversionedCompanyMetricsDocument(testData.populateFullCompanyMetricsDocument());
        initialMetricsDocument.setId(COMPANY_NUMBER);
        initialMetricsDocument.version(1L);

        mongoTemplate.save(initialMetricsDocument);

        when(chargesCountService.recalculateMetrics(any()))
                .thenReturn(MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        Optional<CompanyMetricsDocument> document = companyMetricsRepository.findById(COMPANY_NUMBER);

        assertTrue(document.isPresent());
        assertEquals(2L, document.get().getVersion());

        assertNotNull(document.get().getCompanyMetrics().getMortgage());
        assertNotNull(document.get().getCompanyMetrics().getEtag());
    }


    @Test
    void shouldDeleteDocumentForMissingCompanyNumber() {
        when(chargesCountService.recalculateMetrics(any()))
                .thenReturn(EMPTY_MORTGAGES);

        companyMetricsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER,
                testData.populateMetricsRecalculateApiForCharges());

        Optional<CompanyMetricsDocument> document = companyMetricsRepository.findById(COMPANY_NUMBER);

        assertFalse(document.isPresent());
    }
}
