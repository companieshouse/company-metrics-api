package uk.gov.companieshouse.company.metrics.steps;

import java.util.Collections;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.config.CucumberContext;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;


public class CompanyMetricsApiErrorRetrySteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyMetricsRepository companyMetricsRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    private final ITestUtil iTestUtil = new ITestUtil();

    @When("I send POST request with an invalid payload that fails to de-serialised into Request object")
    public void iSendPOSTRequestWithARandomInvalidPayloadThatFailsToDeSerialisedIntoRequestObject() {
        Object badObject = "A bad object" ;
        HttpEntity<Object> request = new HttpEntity<>(badObject,
            iTestUtil.populateHttpHeaders("1234567"));
        String uri = "/company/1234567/metrics/recalculate";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, request, Void.class );
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send POST request with an no auth header")
    public void i_send_post_request_with_an_no_auth_header() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", "1234567");
        MetricsRecalculateApi metricsRecalculateApi = iTestUtil.populateMetricsRecalculateApi(true);
        HttpEntity<Object> request = new HttpEntity<>(metricsRecalculateApi, headers);
        String uri = "/company/1234567/metrics/recalculate";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, request, Void.class );
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send POST a charges metrics recalculate request {string}")
    public void iSendPOSTRequestWithAnInvalidPayloadThatCausesANPE(String companyNumber) {
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

    @Then("Rest endpoint returns http response code {int} {string} to the client")
    public void rest_endpoint_returns_http_response_code_to_the_client(Integer statusCode, String status) {
        Integer actualStatusCode = (Integer) CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(actualStatusCode).isEqualTo(statusCode);
    }

    @Given("A metrics document exists for {string} without data")
    public void aChargesDocumentExistsForWithoutData(String companyNumber) {
        companyMetricsRepository.save(iTestUtil.createTestCompanyMetricsDocumentWithoutData(companyNumber));
        Assertions.assertThat(companyMetricsRepository.findById(companyNumber).isPresent()).isEqualTo(true);
    }
}
