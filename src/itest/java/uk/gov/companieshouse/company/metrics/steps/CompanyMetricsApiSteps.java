package uk.gov.companieshouse.company.metrics.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import uk.gov.companieshouse.api.charges.ChargeApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.config.CucumberContext;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company.metrics.config.AbstractMongoConfig.mongoDBContainer;

public class CompanyMetricsApiSteps {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChargesRepository chargesRepository;

    @Autowired
    private CompanyMetricsRepository companyMetricsRepository;


    private ITestUtil iTestUtil;

    @Before
    public void dbCleanUp(){
        if (mongoDBContainer.getContainerId() == null) {
            mongoDBContainer.start();
        }
        chargesRepository.deleteAll();
        companyMetricsRepository.deleteAll();
        iTestUtil = new ITestUtil();
    }

    @Given("Company metrics api rest service is running")
    public void charges_delta_consumer_service_is_running() {
        ResponseEntity<String> response = restTemplate.getForEntity("/company-metrics-api/healthcheck", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(200));
        assertThat(response.getBody()).isEqualTo("{\"status\":\"UP\"}");
    }

    @Given("the company charges database is down")
    public void the_company_charges_db_is_down() {
        mongoDBContainer.stop();
    }

    @Given("the company metrics exists for {string}")
    public void the_company_metrics_exists_for(String companyNumber) {
        companyMetricsRepository.save(iTestUtil.createTestCompanyMetricsDocument(companyNumber));
        Assertions.assertThat(companyMetricsRepository.findById(companyNumber).isPresent()).isEqualTo(true);

    }
    @Given("no company metrics exists for {string}")
    public void no_company_metrics_exists_for(String companyNumber) {
        companyMetricsRepository.deleteAll();
        Assertions.assertThat(companyMetricsRepository.findById(companyNumber).isPresent()).isEqualTo(false);
    }
    @Given("the company charges entries exists for {string}")
    public void company_charges_exists_for(String companyNumber) throws IOException {
        chargesRepository.save(iTestUtil.createChargesDocument(companyNumber, "123456789==", ChargeApi.StatusEnum.FULLY_SATISFIED));
        Assertions.assertThat(chargesRepository.getTotalCharges(companyNumber)).isEqualTo(1);
    }

    @Given("multiple company charges entries exists for {string}")
    public void multiple_company_charges_exists_for(String companyNumber) throws IOException {
        chargesRepository.save(iTestUtil.createChargesDocument(companyNumber, "123456789==", ChargeApi.StatusEnum.FULLY_SATISFIED));
        chargesRepository.save(iTestUtil.createChargesDocument(companyNumber, "1234567==", ChargeApi.StatusEnum.SATISFIED));
        chargesRepository.save(iTestUtil.createChargesDocument(companyNumber, "123456==", ChargeApi.StatusEnum.PART_SATISFIED));

        Assertions.assertThat(chargesRepository.getTotalCharges(companyNumber)).isEqualTo(3);
    }

    @When("I send GET request with company number {string}")
    public void i_send_get_request_with_company_number(String companyNumber) {
        String uri = "/company/{company_number}/metrics";
        HttpEntity<?> request = new HttpEntity<>(iTestUtil.populateHttpHeaders("1234567"));
        ResponseEntity<MetricsApi> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                MetricsApi.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @Then("the Get call response body should match {string} file")
    public void the_get_call_response_body_should_match_file(String jsonFileName) throws IOException {

        File file = new ClassPathResource("/json/output/" + jsonFileName + ".json").getFile();
        MetricsApi expected = objectMapper.readValue(file, MetricsApi.class);

        MetricsApi actual = (MetricsApi) CucumberContext.CONTEXT.get("getResponseBody");
        assertThat(actual).isEqualTo(expected);
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        Integer expectedStatusCode = (Integer) CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @When("I send POST request with company number {string}")
    public void i_send_post_request_with_company_number(String companyNumber) throws IOException {

        MetricsRecalculateApi metricsRecalculateApi = iTestUtil.populateMetricsRecalculateApi(true);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");
        HttpEntity<MetricsRecalculateApi> request = new HttpEntity<>(metricsRecalculateApi, headers);

        String uri = "/company/{company_number}/metrics/recalculate";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, request, Void.class, companyNumber);
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());

    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_database() {
        List<CompanyMetricsDocument> companyMetricsDocuments = companyMetricsRepository.findAll();
        Assertions.assertThat(companyMetricsDocuments).hasSize(0);
    }

    @Then("company metrics exists for {string} in company_metrics db with total mortgage count {string}")
    public void company_metrics_exists_for(String companyNumber, String number) throws IOException {
        Assertions.assertThat(companyMetricsRepository.findById(companyNumber).isPresent()).isEqualTo(true);
        CompanyMetricsDocument companyMetricsDocument = companyMetricsRepository.findById(companyNumber).get();
        assertThat(companyMetricsDocument.getId()).isEqualTo(companyNumber);
        assertThat(companyMetricsDocument.getCompanyMetrics().getMortgage().getTotalCount().toString()).isEqualTo(number);

    }

    @When("I send POST request with company number {string} and mortgage flag null in request")
    public void i_send_post_request_with_company_number_and_mortgage_flag_null(String companyNumber) throws IOException {

        MetricsRecalculateApi metricsRecalculateApi = iTestUtil.populateMetricsRecalculateApi(null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");
        HttpEntity<MetricsRecalculateApi> request = new HttpEntity<>(metricsRecalculateApi, headers);

        String uri = "/company/{company_number}/metrics/recalculate";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, request, Void.class, companyNumber);
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());

    }

}
