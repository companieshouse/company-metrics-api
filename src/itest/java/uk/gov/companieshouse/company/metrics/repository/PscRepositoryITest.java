package uk.gov.companieshouse.company.metrics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.Psc;
import uk.gov.companieshouse.company.metrics.model.PscDocument;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

class PscRepositoryITest extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "OC312300";

    @Autowired
    private PscRepository pscRepository;

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
        this.pscRepository.deleteAll();
    }

    @Test
    void shouldReturnAppointmentsForExistingCompanyNumber() {

        List<PscDocument> documents = List.of(
                buildPsc(true),
                buildPsc(false));

        this.pscRepository.saveAll(documents);

        var result = pscRepository.getCounts(COMPANY_NUMBER);

        assertEquals(2, result.getPscsCount());
        assertEquals(1, result.getActivePscsCount());
        assertEquals(1, result.getCeasedPscsCount());
    }


    private PscDocument buildPsc(Boolean isWithdrawn) {
        Psc psc = new Psc();
        if (isWithdrawn) {
            psc.setCeasedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        }
        return new PscDocument()
                .setCompanyNumber(COMPANY_NUMBER)
                .setData(psc);
    }

}
