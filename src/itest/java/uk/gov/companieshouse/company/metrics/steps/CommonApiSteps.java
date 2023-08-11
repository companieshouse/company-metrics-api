package uk.gov.companieshouse.company.metrics.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company.metrics.config.CucumberContext.CONTEXT;

public class CommonApiSteps {

    private ResponseEntity<String> lastResponse;

    private static final String X_REQUEST_ID = "x-request-id";
    private static final String COMPANY_NUMBER = "companyNumber";
    private static final String ORIGINAL_ETAG = "ORIGINAL_ETAG";
    private static final String STATUS_CODE = "statusCode";
    private static final String LOCATION = "LOCATION";
    private static final String RESPONSE_BODY = "RESPONSE";
    private static final String UPDATED_DOC = "UPDATED_DOCUMENT";
    private static final String START_TIME = "START_TIME";
    private static final String COMPANY_METRICS_TYPE = "company_metrics";

    private final CompanyMetricsRepository companyMetricsRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    public CommonApiSteps(CompanyMetricsRepository companyMetricsRepository) {
        this.companyMetricsRepository = companyMetricsRepository;
    }

    @Given("the application running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        lastResponse = null;
    }

    @When("the client invokes {string} endpoint")
    public void theClientInvokesAnEndpoint(String url) {
        lastResponse = restTemplate.getForEntity(url, String.class);
    }

    @Then("the client receives status code of {int}")
    public void theClientReceivesStatusCodeOf(int code) {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.valueOf(code));
    }

    @And("the client receives response body as {string}")
    public void theClientReceivesRawResponse(String response) {
        assertThat(lastResponse.getBody()).isEqualTo(response);
    }

    @Given("A company metrics resource does not exist for a company number of {string}")
    public void companyMetricsResourceDoesNotExistsForTheGivenCompanyNumber(String companyNumber) {
        CONTEXT.set(COMPANY_NUMBER, companyNumber);
        CONTEXT.set(ORIGINAL_ETAG, "");
    }

    @Then("The response code should be HTTP OK")
    public void theResponseCodeShouldBeHttpOK() {
        assertThat(CONTEXT.get(STATUS_CODE)).isEqualTo(HttpStatus.OK.value());
    }

    @And("The location response header should be {string}")
    public void theLocationResponseHeaderShouldBeCompanyCompany_numberMetrics(String uri) {
        assertThat(CONTEXT.get(LOCATION)).isEqualTo(uri);
    }

    @And("The response body should be empty")
    public void theResponseBodyShouldBeEmpty() {
        assertThat(CONTEXT.get(RESPONSE_BODY)).isNull();
    }

    @And("The etag should be updated")
    public void theEtagShouldBeUpdated() {
        CompanyMetricsDocument updatedDocument = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        String eTag = updatedDocument.getCompanyMetrics().getEtag();
        assertThat(eTag)
                .isNotNull()
                .isNotEqualTo(CONTEXT.get(ORIGINAL_ETAG));
    }

    @And("The updated object will have valid timestamp \\(UTC), type: {string} and by: {string}")
    public void theUpdatedObjectWillHaveValidValues(String type, String userId) {
        CompanyMetricsDocument updatedDocument = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        Updated updated = updatedDocument.getUpdated();

        assertThat(updated.getAt()).isAfterOrEqualTo((LocalDateTime) CONTEXT.get(START_TIME));
        assertThat(updated.getBy()).isEqualTo(userId);
        assertThat(updated.getType()).isEqualTo(type);
    }

    @Given("A company metrics resource exists for a company number of {string}")
    public void aCompanyMetricsResourceExistsForACompanyNumberOf(String companyNumber) {
        CONTEXT.set(COMPANY_NUMBER, companyNumber);

        String eTag = UUID.randomUUID().toString();
        CONTEXT.set(ORIGINAL_ETAG, eTag);

        Updated updated = new Updated();
        updated.setAt(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
        updated.setBy("stream-partition-offset");
        updated.setType(COMPANY_METRICS_TYPE);

        CompanyMetricsDocument document = new CompanyMetricsDocument();
        document.setId(companyNumber);
        document.setCompanyMetrics(new MetricsApi()
                .etag(eTag)
                .counts(new CountsApi()
                        .appointments(new AppointmentsApi()
                                .activeCount(0)
                                .activeDirectorsCount(1)
                                .activeLlpMembersCount(0)
                                .totalCount(0)
                                .activeSecretariesCount(0)
                                .resignedCount(0))
                        .personsWithSignificantControl(new PscApi()
                                .totalCount(0)
                                .activePscsCount(0)
                                .activeStatementsCount(0)
                                .pscsCount(0)
                                .statementsCount(0)
                                .ceasedPscsCount(0)
                                .withdrawnStatementsCount(0)))
                .mortgage(new MortgageApi()
                        .partSatisfiedCount(0)
                        .satisfiedCount(1)
                        .totalCount(1)));
        document.setUpdated(updated);
        companyMetricsRepository.save(document);
    }

}
