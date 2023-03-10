package uk.gov.companieshouse.company.metrics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscStatementsRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PscCountsServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";

    private final PscStatementsCounts statementsCounts = new PscStatementsCounts()
            .setStatementsCount(3);

    @Mock
    private Logger logger;

    @Mock
    private PscStatementsRepository statementsRepository;

    @InjectMocks
    private PscCountService pscCountsService;

    @Test
    void shouldRecalculateAppointmentsMetrics() {

        when(statementsRepository.getCounts(COMPANY_NUMBER)).thenReturn(statementsCounts);

        PscApi pscs = pscCountsService.recalculateMetrics(CONTEXT_ID,
                COMPANY_NUMBER);

        assertThat(pscs.getStatementsCount()).isEqualTo(3);
    }

}
