package uk.gov.companieshouse.company.metrics.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "mongodb.company.metrics.collection.name=company_metrics"
})
public class RepositoryITest {

  @Autowired
  private CompanyMetricsRepository companyMetricsRepository;

  private static final String MOCK_COMPANY_NUMBER = "12345678";

  private TestData testData;

  static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
      DockerImageName.parse("mongo:4.0.10"));

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    mongoDBContainer.start();
  }

  @BeforeAll
  static void setup(){
    mongoDBContainer.start();
  }

  @BeforeEach
  void setupForEach() {
    this.companyMetricsRepository.deleteAll();
    this.testData = new TestData();
  }

  @Test
  void should_return_mongodb_as_running() {
    assertTrue(mongoDBContainer.isRunning());
  }

  @AfterAll
  static void tear(){
    mongoDBContainer.stop();
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

}
