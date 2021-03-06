package uk.gov.companieshouse.company.metrics.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.api.charges.ChargeApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.config.TestConfig;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.logging.Logger;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositoryITest extends AbstractIntegrationTest {

  @Autowired
  private CompanyMetricsRepository companyMetricsRepository;

  @Autowired
  private ChargesRepository chargesRepository;

  private static final String MOCK_COMPANY_NUMBER = "12345678";

  private TestData testData;

  private CompanyMetricsService companyMetricsService;

  @Mock
  private Logger logger;

  @BeforeAll
  static void setup(){
    mongoDBContainer.start();
  }

  @BeforeEach
  void setupForEach() {
    this.companyMetricsRepository.deleteAll();
    this.testData = new TestData();
    this.chargesRepository.deleteAll();
    this.companyMetricsService = new CompanyMetricsService(logger, companyMetricsRepository, chargesRepository);
  }

  @AfterAll
  static void tear(){
    mongoDBContainer.stop();
  }

  @Test
  void should_return_mongodb_as_running() {
    assertTrue(mongoDBContainer.isRunning());
  }

  @Test
  void should_return_company_metrics_for_existing_company_number() throws IOException {
    MetricsApi metricsApi = testData.
            createMetricsApi("source-metrics-body-1.json");

    Updated updated = testData.
            createUpdated("source-metrics-updated-body-1.json");
    CompanyMetricsDocument expectedCompanyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);
    expectedCompanyMetricsDocument.setId(MOCK_COMPANY_NUMBER);

    this.companyMetricsRepository.save(expectedCompanyMetricsDocument);
    Optional<CompanyMetricsDocument> companyMetricsDocument  = this.companyMetricsRepository.findById(MOCK_COMPANY_NUMBER);
    assertTrue(companyMetricsDocument.isPresent());
    assertThat(companyMetricsDocument.get()).usingRecursiveComparison().isEqualTo(expectedCompanyMetricsDocument);
  }

  @Test
  void should_return_no_company_metrics_for_non_existing_company_number() throws IOException {
    MetricsApi metricsApi = testData.
            createMetricsApi("source-metrics-body-1.json");

    Updated updated = testData.
            createUpdated("source-metrics-updated-body-1.json");
    CompanyMetricsDocument expectedCompanyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);
    expectedCompanyMetricsDocument.setId(MOCK_COMPANY_NUMBER);

    this.companyMetricsRepository.save(expectedCompanyMetricsDocument);
    assertTrue(this.companyMetricsRepository.findById("non_existing_company_number").isEmpty());
  }

  @Test
  void should_return_mortgages_for_existing_company_number() throws IOException {

    List<ChargesDocument> documentList =  new ArrayList<>();
    documentList.add(populateChargesDocument("1234", MOCK_COMPANY_NUMBER, ChargeApi.StatusEnum.FULLY_SATISFIED));
    documentList.add(populateChargesDocument("12345", MOCK_COMPANY_NUMBER, ChargeApi.StatusEnum.SATISFIED));
    documentList.add(populateChargesDocument("123456", MOCK_COMPANY_NUMBER, ChargeApi.StatusEnum.PART_SATISFIED));
    documentList.add(populateChargesDocument("1234567", MOCK_COMPANY_NUMBER, ChargeApi.StatusEnum.OUTSTANDING));

    this.chargesRepository.saveAll(documentList);

     assertEquals(4, this.chargesRepository.getTotalCharges(MOCK_COMPANY_NUMBER));
     assertEquals(2, this.chargesRepository.getSatisfiedAndFullSatisfiedCharges(MOCK_COMPANY_NUMBER,"satisfied","fully-satisfied"));
     assertEquals(1, this.chargesRepository.getPartSatisfiedCharges(MOCK_COMPANY_NUMBER,"part-satisfied"));

  }

  @Test
  void should_update_company_metrics_collection_with_mortgage_details() throws IOException {

    MetricsApi metricsApi = testData.
            createMetricsApi("source-metrics-body-1.json");
    Updated updated = testData.
            createUpdated("source-metrics-updated-body-1.json");
    CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);
    companyMetricsDocument.setId(MOCK_COMPANY_NUMBER);
    companyMetricsService.upsertMetrics("1234", 4,1,1,"updatedBy", companyMetricsDocument );

    Optional<CompanyMetricsDocument>  document = companyMetricsRepository.findById(MOCK_COMPANY_NUMBER);
    assertEquals( MOCK_COMPANY_NUMBER, document.get().getId());
    assertEquals( 4, document.get().getCompanyMetrics().getMortgage().getTotalCount());
    assertEquals( 1, document.get().getCompanyMetrics().getMortgage().getSatisfiedCount());
    assertEquals( 1, document.get().getCompanyMetrics().getMortgage().getPartSatisfiedCount());

  }

  @Test
  void should_insert_company_metrics_collection_with_mortgage_details() throws IOException {

    companyMetricsService.insertMetrics("1234", MOCK_COMPANY_NUMBER,4,1,1,"updatedBy" );

    Optional<CompanyMetricsDocument>  document = companyMetricsRepository.findById(MOCK_COMPANY_NUMBER);
    assertEquals( MOCK_COMPANY_NUMBER, document.get().getId());
    assertEquals( 4, document.get().getCompanyMetrics().getMortgage().getTotalCount());

  }

  private ChargesDocument populateChargesDocument(String id, String companyNumber, ChargeApi.StatusEnum status) {

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
