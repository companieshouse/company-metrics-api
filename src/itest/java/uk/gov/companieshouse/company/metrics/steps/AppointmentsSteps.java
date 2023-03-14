package uk.gov.companieshouse.company.metrics.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company.metrics.config.AbstractMongoConfig.mongoDBContainer;
import static uk.gov.companieshouse.company.metrics.config.CucumberContext.CONTEXT;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.AppointmentDocument;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Officer;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.appointments.AppointmentRepository;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;

public class AppointmentsSteps {

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

    private final TestRestTemplate restTemplate;

    private final HttpHeaders headers = new HttpHeaders();

    public AppointmentsSteps(ChargesRepository chargesRepository,
            CompanyMetricsRepository companyMetricsRepository,
            AppointmentRepository appointmentRepository, TestRestTemplate restTemplate) {
        this.chargesRepository = chargesRepository;
        this.companyMetricsRepository = companyMetricsRepository;
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
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

        CONTEXT.set(START_TIME, LocalDateTime.now());
    }



    @And("The user is authenticated and authorised with internal app privileges")
    public void userIsAuthenticatedAndAuthorisedWithInternalAppPrivileges() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ERIC_IDENTITY, "SOME_IDENTITY");
        headers.set(ERIC_IDENTITY_TYPE, IDENTITY_TYPE_KEY);
        headers.set(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_APP_PRIVILEGES);
    }

    @When("The recalculate endpoint is called with a context ID of {string} but without an updatedBy value")
    public void theRecalculateEndpointIsCalledWithoutUpdatedBy(String contextId) {
        callRecalculateEndpoint(null, contextId);
    }


    @When("The recalculate endpoint is called with updatedBy as {string}")
    public void theRecalculateEndpointIsCalled(String updatedBy) {
        String contextId = UUID.randomUUID().toString();
        callRecalculateEndpoint(updatedBy, contextId);
    }

    @And("The response should not include a location header")
    public void theResponseShouldNotIncludeALocationHeader() {
        assertThat(CONTEXT.get(LOCATION)).isNull();
    }


    @And("The metrics resource has a count.appointments object")
    public void aNewMetricsResourceWithACountAppointmentsObjectShouldBeCreated() {
        CompanyMetricsDocument updatedDocument = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(updatedDocument.getCompanyMetrics()).isNotNull();
        assertThat(updatedDocument.getCompanyMetrics().getCounts()).isNotNull();
        assertThat(updatedDocument.getCompanyMetrics().getCounts().getAppointments()).isNotNull();
    }


    @And("The company has ACTIVE appointments for {int} directors, {int} secretaries, {int} LLP members, {int} Corp. LLP members")
    public void theCompanyHasACTIVEAppointmentsFor(int activeDirectors, int activeSecretaries,
            int activeLLPMembers, int activeCorporateLLPMembers) {
        String companyNumber = CONTEXT.get(COMPANY_NUMBER).toString();

        List<AppointmentDocument> documents = new ArrayList<>();
        for (int i = 0; i < activeDirectors; i++) {
            documents.add(buildAppointment(companyNumber, DIRECTOR));
        }
        for (int i = 0; i < activeSecretaries; i++) {
            documents.add(buildAppointment(companyNumber, SECRETARY));
        }
        for (int i = 0; i < activeLLPMembers; i++) {
            documents.add(buildAppointment(companyNumber, LLP_MEMBER));
        }
        for (int i = 0; i < activeCorporateLLPMembers; i++) {
            documents.add(buildAppointment(companyNumber, CORPORATE_LLP_MEMBER));
        }

        appointmentRepository.saveAll(documents);
    }

    @And("The company has RESIGNED appointments for {int} directors, {int} secretaries, {int} LLP members, {int} Corp. LLP members")
    public void theCompanyHasRESIGNEDAppointmentsFor(int resignedDirectors, int resignedSecretaries,
            int resignedLLPMembers, int resignedCorporateLLPMembers) {
        String companyNumber = CONTEXT.get(COMPANY_NUMBER).toString();

        List<AppointmentDocument> documents = new ArrayList<>();
        for (int i = 0; i < resignedDirectors; i++) {
            documents.add(buildResignedAppointment(companyNumber, DIRECTOR));
        }
        for (int i = 0; i < resignedSecretaries; i++) {
            documents.add(buildResignedAppointment(companyNumber, SECRETARY));
        }
        for (int i = 0; i < resignedLLPMembers; i++) {
            documents.add(buildResignedAppointment(companyNumber, LLP_MEMBER));
        }
        for (int i = 0; i < resignedCorporateLLPMembers; i++) {
            documents.add(buildResignedAppointment(companyNumber, CORPORATE_LLP_MEMBER));
        }

        appointmentRepository.saveAll(documents);
    }

    @And("The company has ZERO appointments registered")
    public void theCompanyHasZEROAppointmentsRegistered() {
        appointmentRepository.deleteAll();

    }

    @And("The appointments should have {int} active directors, {int} active secretaries, {int} active LLP members, {int} active Corp. LLP members, {int} total appointments, {int} active appointments, {int} resigned appointments")
    public void theAppointmentsShouldHaveActiveOfficerCounts(int activeDirectors,
            int activeSecretaries, int activeLLPMembers, int activeCorporateLLPMembers,
            int totalCount, int activeCount, int resignedCount) {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        AppointmentsApi appointments = document.getCompanyMetrics().getCounts().getAppointments();

        assertThat(appointments.getTotalCount()).isEqualTo(totalCount);
        assertThat(appointments.getActiveCount()).isEqualTo(activeCount);
        assertThat(appointments.getResignedCount()).isEqualTo(resignedCount);
        assertThat(appointments.getActiveDirectorsCount()).isEqualTo(activeDirectors);
        assertThat(appointments.getActiveSecretariesCount()).isEqualTo(activeSecretaries);
        assertThat(appointments.getActiveLlpMembersCount()).isEqualTo(
                activeLLPMembers + activeCorporateLLPMembers);
    }

    @Given("A company metrics resource exists for a company number of {string} but with no counts metrics")
    public void aCompanyMetricsResourceExistsButNoAppointmentCounts(String companyNumber) {
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
                .mortgage(new MortgageApi()
                        .partSatisfiedCount(0)
                        .satisfiedCount(1)
                        .totalCount(1)));
        document.setUpdated(updated);
        companyMetricsRepository.save(document);
    }

    @Given("A company metrics resource exists for a company number of {string} with appointment counts that are all zero")
    public void aCompanyMetricsResourceExistsButAppointmentCountsAllZero(String companyNumber) {
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
                        .appointments(new AppointmentsApi()
                                .activeCount(0)
                                .activeDirectorsCount(1)
                                .activeLlpMembersCount(0)
                                .totalCount(0)
                                .activeSecretariesCount(0)
                                .resignedCount(0)))
                .mortgage(new MortgageApi()
                        .partSatisfiedCount(0)
                        .satisfiedCount(1)
                        .totalCount(1)));
        document.setUpdated(updated);
        companyMetricsRepository.save(document);
    }

    @And("The metrics resource does not have a count.appointments object")
    public void theMetricsResourceDoesNotHaveACountAppointmentsObject() {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(document.getCompanyMetrics().getCounts()).isNotNull();
        assertThat(document.getCompanyMetrics().getCounts().getAppointments()).isNull();
    }

    @And("The metrics resource does not have a counts object")
    public void theMetricsResourceDoesNotHaveACountObject() {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(document.getCompanyMetrics().getCounts()).isNull();
    }

    @Given("A company metrics resource exists for a company number of {string} but no metrics")
    public void aCompanyMetricsResourceExistsForACompanyNumberOfButNoMetrics(String companyNumber) {
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
                .etag(eTag));
        document.setUpdated(updated);
        companyMetricsRepository.save(document);
    }

    @And("The company metrics document has been deleted")
    public void theCompanyMetricsDocumentDoesNotHaveAMetricsObject() {
        CompanyMetricsDocument document = (CompanyMetricsDocument) CONTEXT.get(UPDATED_DOC);
        assertThat(document).isNull();
    }

    public void callRecalculateEndpoint(String updatedBy, String contextId) {
        CONTEXT.set(CONTEXT_ID, contextId);
        headers.set(X_REQUEST_ID, contextId);

        MetricsRecalculateApi requestBody = new MetricsRecalculateApi()
                .appointments(true)
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


    private AppointmentDocument buildAppointment(String companyNumber, String role) {
        Officer officer = new Officer()
                .setOfficerRole(role);

        return new AppointmentDocument()
                .setCompanyNumber(companyNumber)
                .setData(officer);
    }

    private AppointmentDocument buildResignedAppointment(String companyNumber, String role) {

        AppointmentDocument appointmentDocument = buildAppointment(companyNumber, role);
        appointmentDocument.getData()
                .setResignedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        return appointmentDocument;
    }
}
