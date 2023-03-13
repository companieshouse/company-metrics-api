package uk.gov.companieshouse.company.metrics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
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
            .setStatementsCount(3)
            .setActiveStatementsCount(2)
            .setWithdrawnStatementsCount(1);

    private final PscStatementsCounts noStatementsCounts = new PscStatementsCounts()
            .setStatementsCount(0)
            .setActiveStatementsCount(0)
            .setWithdrawnStatementsCount(0);

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
        assertThat(pscs.getActiveStatementsCount()).isEqualTo(2);
        assertThat(pscs.getWithdrawnStatementsCount()).isEqualTo(1);
    }

    @Test
    void shouldRemovePscsMetricsWhenPscsCountsAreZero() {
        when(statementsRepository.getCounts(COMPANY_NUMBER)).thenReturn(noStatementsCounts);

        PscApi pscs = pscCountsService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER);

        assertThat(pscs.getStatementsCount()).isEqualTo(0);
        assertThat(pscs.getActiveStatementsCount()).isEqualTo(0);
        assertThat(pscs.getWithdrawnStatementsCount()).isEqualTo(0);
    }

}
