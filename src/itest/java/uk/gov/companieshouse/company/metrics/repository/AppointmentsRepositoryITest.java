package uk.gov.companieshouse.company.metrics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.AppointmentDocument;
import uk.gov.companieshouse.company.metrics.model.Officer;
import uk.gov.companieshouse.company.metrics.repository.metrics.AppointmentRepository;

public class AppointmentsRepositoryITest extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "OC312300";
    private static final String DIRECTOR = "director";
    private static final String CORPORATE_LLP_MEMBER = "corporate-llp-member";
    private static final String SECRETARY = "secretary";
    private static final String LLP_MEMBER = "llp-member";

    @Autowired
    private AppointmentRepository appointmentsRepository;

    Optional resignationDate = Optional.of(Instant.now().minus(10, ChronoUnit.DAYS));

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
                buildAppointment(COMPANY_NUMBER, DIRECTOR, Optional.empty()),
                buildAppointment(COMPANY_NUMBER, DIRECTOR, Optional.empty()),
                buildAppointment(COMPANY_NUMBER, SECRETARY, Optional.empty()),
                buildAppointment(COMPANY_NUMBER, LLP_MEMBER, Optional.empty()),
                buildAppointment(COMPANY_NUMBER, CORPORATE_LLP_MEMBER, Optional.empty()),
                buildAppointment(COMPANY_NUMBER, DIRECTOR, resignationDate));

        this.appointmentsRepository.saveAll(documents);

        var result = appointmentsRepository.getCounts(COMPANY_NUMBER);

        assertEquals(6, result.getTotalCount());
        assertEquals(5, result.getActiveCount());
        assertEquals(2, result.getActiveDirectorsCount());
        assertEquals(1, result.getActiveSecretariesCount());
        assertEquals(2, result.getActiveLlpMembersCount());
        assertEquals(1, result.getResignedCount());
    }

    private AppointmentDocument buildAppointment(String companyNumber, String role,
            Optional<Instant> resigned) {

        Officer officer = new Officer();
        officer.setOfficerRole(role);
        officer.setCompanyNumber(companyNumber);
        resigned.ifPresent(date -> officer.setResignedOn(date));

        return new AppointmentDocument()
                .setCompanyNumber(companyNumber)
                .setData(officer);
    }
}
