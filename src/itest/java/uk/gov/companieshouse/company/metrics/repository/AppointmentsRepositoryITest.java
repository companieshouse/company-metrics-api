package uk.gov.companieshouse.company.metrics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.AppointmentDocument;
import uk.gov.companieshouse.company.metrics.model.Officer;
import uk.gov.companieshouse.company.metrics.repository.appointments.AppointmentRepository;

class AppointmentsRepositoryITest extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "OC312300";
    private static final String DIRECTOR = "director";
    private static final String CORPORATE_LLP_MEMBER = "corporate-llp-member";
    private static final String SECRETARY = "secretary";
    private static final String LLP_MEMBER = "llp-member";

    @Autowired
    private AppointmentRepository appointmentsRepository;

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
        this.appointmentsRepository.deleteAll();
    }

    @Test
    void shouldReturnAppointmentsForExistingCompanyNumber() {

        List<AppointmentDocument> documents = List.of(
                buildAppointment(DIRECTOR),
                buildAppointment(DIRECTOR),
                buildAppointment(SECRETARY),
                buildAppointment(LLP_MEMBER),
                buildAppointment(CORPORATE_LLP_MEMBER),
                buildResignedAppointment(DIRECTOR));

        this.appointmentsRepository.saveAll(documents);

        var result = appointmentsRepository.getCounts(COMPANY_NUMBER);

        assertEquals(6, result.getTotalCount());
        assertEquals(5, result.getActiveCount());
        assertEquals(2, result.getActiveDirectorsCount());
        assertEquals(1, result.getActiveSecretariesCount());
        assertEquals(2, result.getActiveLlpMembersCount());
        assertEquals(1, result.getResignedCount());
    }

    private AppointmentDocument buildAppointment(String role) {

        Officer officer = new Officer()
                .setOfficerRole(role);

        return new AppointmentDocument()
                .setCompanyNumber(COMPANY_NUMBER)
                .setData(officer);
    }

    private AppointmentDocument buildResignedAppointment(String role) {

        AppointmentDocument appointmentDocument = buildAppointment(role);
        appointmentDocument.getData()
                .setResignedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        return appointmentDocument;
    }
}
