package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.model.ChargesCounts;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class ChargesCountServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    public static final String CONTEXT_ID = "12345";

    private final ChargesCounts chargesCounts = new ChargesCounts()
            .setTotalCount(30)
            .setPartSatisfied(20)
            .setSatisfiedOrFullySatisfied(10);

    @Mock
    private Logger logger;

    @Mock
    private ChargesRepository chargesRepository;

    @InjectMocks
    private ChargesCountService chargesCountService;

    @Test
    void shouldRecalculateMetricsWhenMortgageApiElementExists() {

        when(chargesRepository.getCounts(COMPANY_NUMBER)).thenReturn(chargesCounts);

        MortgageApi mortgages = chargesCountService.recalculateMetrics(COMPANY_NUMBER);

        assertThat(mortgages.getTotalCount()).isEqualTo(30);
        assertThat(mortgages.getSatisfiedCount()).isEqualTo(10);
        assertThat(mortgages.getPartSatisfiedCount()).isEqualTo(20);
    }

    @Test
    void shouldRecalculateMetricsWhenMortgageApiElementDoesNotExists() {
        when(chargesRepository.getCounts(COMPANY_NUMBER)).thenReturn(chargesCounts);

        MortgageApi mortgages = chargesCountService.recalculateMetrics(COMPANY_NUMBER);

        assertThat(mortgages.getTotalCount()).isEqualTo(30);
        assertThat(mortgages.getSatisfiedCount()).isEqualTo(10);
        assertThat(mortgages.getPartSatisfiedCount()).isEqualTo(20);
    }
}