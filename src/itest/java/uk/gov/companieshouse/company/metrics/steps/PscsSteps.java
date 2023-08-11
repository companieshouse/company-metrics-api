package uk.gov.companieshouse.company.metrics.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company.metrics.config.AbstractMongoConfig.mongoDBContainer;
import static uk.gov.companieshouse.company.metrics.config.CucumberContext.CONTEXT;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Psc;
import uk.gov.companieshouse.company.metrics.model.PscDocument;
import uk.gov.companieshouse.company.metrics.model.PscStatement;
import uk.gov.companieshouse.company.metrics.model.PscStatementDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.appointments.AppointmentRepository;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscRepository;
import uk.gov.companieshouse.company.metrics.repository.pscstatements.PscStatementsRepository;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PscsSteps {

    private static final String RECALCULATE_URI = "/company/{company_number}/metrics/recalculate";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String IDENTITY_TYPE_KEY = "key";
    private static final String INTERNAL_APP_PRIVILEGES = "internal-app";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String COMPANY_NUMBER = "companyNumber";
    private static final String STATUS_CODE = "statusCode";
    private static final String LOCATION = "LOCATION";
    private static final String RESPONSE_BODY = "RESPONSE";
    private static final String UPDATED_DOC = "UPDATED_DOCUMENT";
    private static final String ORIGINAL_ETAG = "ORIGINAL_ETAG";
    private static final String DIRECTOR = "director";
    private static final String CORPORATE_LLP_MEMBER = "corporate-llp-member";
    private static final String SECRETARY = "secretary";
    private static final String LLP_MEMBER = "llp-member";
    private static final String START_TIME = "START_TIME";
    private static final String CONTEXT_ID = "CONTEXT_ID";
    private static final String COMPANY_METRICS_TYPE = "company_metrics";

    private final ChargesRepository chargesRepository;

    private final CompanyMetricsRepository companyMetricsRepository;

    private final AppointmentRepository appointmentRepository;

    private final PscStatementsRepository pscStatementsRepository;

    private final PscRepository pscRepository;

    private final TestRestTemplate restTemplate;

    private final HttpHeaders headers = new HttpHeaders();

    public PscsSteps(ChargesRepository chargesRepository,
                     CompanyMetricsRepository companyMetricsRepository,
                     AppointmentRepository appointmentRepository,
                     PscStatementsRepository pscStatementsRepository,
                     PscRepository pscRepository,
                     TestRestTemplate restTemplate) {
        this.chargesRepository = chargesRepository;
        this.companyMetricsRepository = companyMetricsRepository;
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
        this.pscStatementsRepository = pscStatementsRepository;
        this.pscRepository = pscRepository;
    }

    @Before
    public void setup() {
        CONTEXT.clear();

        if (mongoDBContainer.getContainerId() == null) {
            mongoDBContainer.start();
        }
        chargesRepository.deleteAll();
        companyMetricsRepository.deleteAll();
        appointmentRepository.deleteAll();
        pscRepository.deleteAll();
        pscStatementsRepository.deleteAll();

        CONTEXT.set(START_TIME, LocalDateTime.now());
    }

    @Given("No Company Metrics resource exists for a company number of {string}")
    public void companyMetricsResourceDoesNotExistsForTheGivenCompanyNumber(String companyNumber) {
        CONTEXT.set(COMPANY_NUMBER, companyNumber);
        CONTEXT.set(ORIGINAL_ETAG, "");
    }

    @And("The user is fully authenticated and authorised with internal app privileges")
    public void userIsAuthenticatedAndAuthorisedWithInternalAppPrivileges() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ERIC_IDENTITY, "SOME_IDENTITY");
        headers.set(ERIC_IDENTITY_TYPE, IDENTITY_TYPE_KEY);
        headers.set(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_APP_PRIVILEGES);
    }

    @When("The recalculate Pscs endpoint is called with a context ID of {string} but without an updatedBy value")
    public void theRecalculateEndpointIsCalledWithoutUpdatedBy(String contextId) {
        callRecalculateEndpoint(null, contextId);
    }


    @When("The recalculate Pscs endpoint is called with updatedBy as {string}")
    public void theRecalculateEndpointIsCalled(String updatedBy) {
        String contextId = UUID.randomUUID().toString();
        callRecalculateEndpoint(updatedBy, contextId);
    }

    @And("The company has ZERO Pscs or Psc Statements registered")
    public void theCompanyHasZEROAppointmentsRegistered() {
        pscRepository.deleteAll();
        pscStatementsRepository.deleteAll();
    }

    @And("The metrics resource has a count.persons-with-significant-control object")
    public void aNewMetricsResourceWithACountPscsObjectShouldBeCreated() {
        CompanyMetricsDocument updatedDocument = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(updatedDocument.getCompanyMetrics()).isNotNull();
        assertThat(updatedDocument.getCompanyMetrics().getCounts()).isNotNull();
        assertThat(updatedDocument.getCompanyMetrics().getCounts().getPersonsWithSignificantControl()).isNotNull();
    }

    @And("The company has {int} active Psc Statements and {int} withdrawn Psc Statements")
    public void theCompanyHasPscStatements(int activeStatements, int withdrawnStatements) {

        List<PscStatementDocument> documents = new ArrayList<>();
        for (int i = 0; i < activeStatements; i++) {
            documents.add(buildStatement(false));
        }
        for (int i = 0; i < withdrawnStatements; i++) {
            documents.add(buildStatement(true));
        }

        pscStatementsRepository.saveAll(documents);
    }

    @And("The company has {int} active Pscs and {int} ceased Pscs")
    public void theCompanyHasPscs(int activePscs, int ceasedPscs) {

        List<PscDocument> documents = new ArrayList<>();
        for (int i = 0; i < activePscs; i++) {
            documents.add(buildPsc(false));
        }
        for (int i = 0; i < ceasedPscs; i++) {
            documents.add(buildPsc(true));
        }

        pscRepository.saveAll(documents);
    }

    @And("The PscCounts should have {int} active statements, {int} withdrawn statements, {int} active Pscs, {int} ceased Pscs and the correct counts for total Pscs and total statements")
    public void thePscsShouldHaveCorrectCounts(int activeStatements, int withdrawnStatements, int activePscs, int ceasedPscs) {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        PscApi pscs = document.getCompanyMetrics().getCounts().getPersonsWithSignificantControl();

        int totalStatements = activeStatements + withdrawnStatements;
        int totalPscs = activePscs + ceasedPscs;
        int totalCount = totalStatements + totalPscs;
        assertThat(pscs.getTotalCount()).isEqualTo(totalCount);
        assertThat(pscs.getStatementsCount()).isEqualTo(totalStatements);
        assertThat(pscs.getActiveStatementsCount()).isEqualTo(activeStatements);
        assertThat(pscs.getWithdrawnStatementsCount()).isEqualTo(withdrawnStatements);
        assertThat(pscs.getPscsCount()).isEqualTo(totalPscs);
        assertThat(pscs.getActivePscsCount()).isEqualTo(activePscs);
        assertThat(pscs.getCeasedPscsCount()).isEqualTo(ceasedPscs);
    }

    @Given("A company metrics resource exists for a company number of {string} with psc counts that are all zero")
    public void aCompanyMetricsResourceExistsButPscCountsAllZero(String companyNumber) {
        CONTEXT.set(COMPANY_NUMBER, companyNumber);

        String eTag = UUID.randomUUID().toString();
        CONTEXT.set(ORIGINAL_ETAG, eTag);

        Updated updated = new Updated();
        updated.setAt(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
        updated.setBy("Someone");
        updated.setType(COMPANY_METRICS_TYPE);

        CompanyMetricsDocument document = new CompanyMetricsDocument();
        document.setId(companyNumber);
        document.setCompanyMetrics(new MetricsApi()
                .etag(eTag)
                .counts(new CountsApi()
                        .personsWithSignificantControl(new PscApi()
                                .totalCount(0)
                                .statementsCount(1)
                                .activeStatementsCount(0)
                                .withdrawnStatementsCount(0)
                                .pscsCount(0)
                                .ceasedPscsCount(0)
                                .activePscsCount(0)))
                .mortgage(new MortgageApi()
                        .partSatisfiedCount(0)
                        .satisfiedCount(1)
                        .totalCount(1)));
        document.setUpdated(updated);
        companyMetricsRepository.save(document);
    }

    @And("The metrics resource does not have a count.persons-with-significant-control object")
    public void theMetricsResourceDoesNotHaveACountPscsObject() {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(document.getCompanyMetrics().getCounts()).isNotNull();
        assertThat(document.getCompanyMetrics().getCounts().getPersonsWithSignificantControl()).isNull();
    }

    public void callRecalculateEndpoint(String updatedBy, String contextId) {
        CONTEXT.set(CONTEXT_ID, contextId);
        headers.set(X_REQUEST_ID, contextId);

        MetricsRecalculateApi requestBody = new MetricsRecalculateApi()
                .personsWithSignificantControl(true)
                .internalData(new InternalData()
                        .updatedBy(updatedBy));

        HttpEntity<MetricsRecalculateApi> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Void> response = restTemplate.exchange(RECALCULATE_URI, HttpMethod.POST,
                request, Void.class, CONTEXT.get(COMPANY_NUMBER));
        CONTEXT.set(RESPONSE_BODY, response.getBody());
        CONTEXT.set(STATUS_CODE, response.getStatusCodeValue());

        URI location = response.getHeaders().getLocation();
        if (location != null) {
            CONTEXT.set(LOCATION, location.toString());
        }

        CompanyMetricsDocument updatedDocument = companyMetricsRepository
                .findById(CONTEXT.get(COMPANY_NUMBER).toString()).orElse(null);
        CONTEXT.set(UPDATED_DOC, updatedDocument);
    }

    private PscDocument buildPsc(Boolean isWithdrawn) {
        Psc psc = new Psc();
        if (isWithdrawn) {
            psc.setCeasedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        }
        return new PscDocument()
                .setCompanyNumber(CONTEXT.get(COMPANY_NUMBER).toString())
                .setData(psc);
    }

    private PscStatementDocument buildStatement(Boolean isWithdrawn) {
        PscStatement statement = new PscStatement();
        if (isWithdrawn) {
            statement.setCeasedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        }
        return new PscStatementDocument()
                .setCompanyNumber(CONTEXT.get(COMPANY_NUMBER).toString())
                .setData(statement);
    }

}
