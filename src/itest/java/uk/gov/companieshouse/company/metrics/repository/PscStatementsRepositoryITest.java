package uk.gov.companieshouse.company.metrics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.api.model.statements.StatementApi;
import uk.gov.companieshouse.company.metrics.AbstractIntegrationTest;
import uk.gov.companieshouse.company.metrics.model.PscStatement;
import uk.gov.companieshouse.company.metrics.model.PscStatementDocument;
import uk.gov.companieshouse.company.metrics.repository.pscstatements.PscStatementsRepository;

import java.time.Instant;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PscStatementsRepositoryITest extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "OC312300";

    @Autowired
    private PscStatementsRepository pscStatementsRepository;

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
        this.pscStatementsRepository.deleteAll();
    }

    @Test
    void shouldReturnAppointmentsForExistingCompanyNumber() {

        List<PscStatementDocument> documents = List.of(
                buildStatement(true),
                buildStatement(false));

        this.pscStatementsRepository.saveAll(documents);

        var result = pscStatementsRepository.getCounts(COMPANY_NUMBER);

        assertEquals(2, result.getStatementsCount());
        assertEquals(1, result.getActiveStatementsCount());
        assertEquals(1, result.getWithdrawnStatementsCount());
    }


    private PscStatementDocument buildStatement(Boolean isWithdrawn) {
        PscStatement statement = new PscStatement();
        if (isWithdrawn) {
            statement.setCeasedOn(Instant.now().minus(10, ChronoUnit.DAYS));
        }
        return new PscStatementDocument()
                .setCompanyNumber(COMPANY_NUMBER)
                .setData(statement);
    }

}
