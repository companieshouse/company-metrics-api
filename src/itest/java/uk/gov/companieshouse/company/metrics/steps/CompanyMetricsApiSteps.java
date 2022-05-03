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
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.config.CucumberContext;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

import java.io.File;
import java.io.IOException;

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
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        chargesRepository.deleteAll();
        companyMetricsRepository.deleteAll();
        iTestUtil = new ITestUtil();
    }

    @Given("the company charges database is down")
    public void the_company_charges_db_is_down() {
        mongoDBContainer.stop();
    }


    @Given("the company metrics exists for {string}")
    public void the_company_links_exists_for(String dataFile) throws IOException {
        companyMetricsRepository.save(iTestUtil.populateCompanyMetricsDocument(dataFile));
    }

    @When("I send GET request with company number {string}")
    public void i_send_get_request_with_company_number(String companyNumber) {
        String uri = "/company/{company_number}/metrics";
        ResponseEntity<MetricsApi> response = restTemplate.exchange(uri, HttpMethod.GET, null,
                MetricsApi.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @Then("the Get call response body should match {string} file")
    public void the_get_call_response_body_should_match_file(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();

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
        HttpEntity<MetricsRecalculateApi> request = new HttpEntity<>(metricsRecalculateApi,
                iTestUtil.populateHttpHeaders("1234567"));

        String uri = "/company/{company_number}/metrics/recalculate";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, request, Void.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());

    }

}
