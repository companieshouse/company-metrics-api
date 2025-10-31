package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.PscStatementsCounts;
import uk.gov.companieshouse.company.metrics.model.PscsCounts;
import uk.gov.companieshouse.company.metrics.repository.pscs.PscRepository;
import uk.gov.companieshouse.company.metrics.repository.pscstatements.PscStatementsRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PscCountsServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "12345";

    private final PscStatementsCounts statementsCounts = new PscStatementsCounts()
            .setStatementsCount(3)
            .setActiveStatementsCount(2)
            .setWithdrawnStatementsCount(1);

    private final PscsCounts pscsCounts = new PscsCounts()
            .setPscsCount(3)
            .setActivePscsCount(2)
            .setCeasedPscsCount(1);

    private final PscStatementsCounts noStatementsCounts = new PscStatementsCounts()
            .setStatementsCount(0)
            .setActiveStatementsCount(0)
            .setWithdrawnStatementsCount(0);

    private final PscsCounts noPscsCounts = new PscsCounts()
            .setPscsCount(0)
            .setActivePscsCount(0)
            .setCeasedPscsCount(0);

    @Mock
    private Logger logger;

    @Mock
    private PscStatementsRepository statementsRepository;
    @Mock
    private PscRepository pscRepository;

    @InjectMocks
    private PscCountService pscCountsService;

    @Test
    void shouldRecalculateAppointmentsMetrics() {

        when(statementsRepository.getCounts(COMPANY_NUMBER)).thenReturn(statementsCounts);
        when(pscRepository.getCounts(COMPANY_NUMBER)).thenReturn(pscsCounts);

        PscApi pscs = pscCountsService.recalculateMetrics(COMPANY_NUMBER);

        assertThat(pscs.getStatementsCount()).isEqualTo(3);
        assertThat(pscs.getActiveStatementsCount()).isEqualTo(2);
        assertThat(pscs.getWithdrawnStatementsCount()).isEqualTo(1);
        assertThat(pscs.getPscsCount()).isEqualTo(3);
        assertThat(pscs.getActivePscsCount()).isEqualTo(2);
        assertThat(pscs.getCeasedPscsCount()).isEqualTo(1);
        assertThat(pscs.getTotalCount()).isEqualTo(6);
    }

    @Test
    void shouldRemovePscsMetricsWhenPscsCountsAreZero() {
        when(statementsRepository.getCounts(COMPANY_NUMBER)).thenReturn(noStatementsCounts);
        when(pscRepository.getCounts(COMPANY_NUMBER)).thenReturn(noPscsCounts);

        PscApi pscs = pscCountsService.recalculateMetrics(COMPANY_NUMBER);

        assertThat(pscs.getStatementsCount()).isZero();
        assertThat(pscs.getActiveStatementsCount()).isZero();
        assertThat(pscs.getWithdrawnStatementsCount()).isZero();
        assertThat(pscs.getPscsCount()).isZero();
        assertThat(pscs.getActivePscsCount()).isZero();
        assertThat(pscs.getPscsCount()).isZero();
        assertThat(pscs.getTotalCount()).isZero();
    }

}
