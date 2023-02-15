package uk.gov.companieshouse.company.metrics.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ChargesCountServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    public static final String CONTEXT_ID = "12345";
    public static final String E_TAG = "etag";
    private static final String SATISFIED_STATUS = "satisfied";
    private static final String FULLY_SATISFIED_STATUS = "fully-satisfied";
    private static final String PART_SATISFIED_STATUS = "part-satisfied";

    @Mock
    Logger logger;

    @Mock
    ChargesRepository chargesRepository;

    @InjectMocks
    ChargesCountService chargesCountService;

    @Test
    void shouldRecalculateMetricsWhenMortgageApiElementExists() {
        MetricsApi metricsApi = new MetricsApi();
        metricsApi.setEtag(E_TAG);

        MortgageApi mortgageApi = new MortgageApi();
        mortgageApi.setTotalCount(1);
        mortgageApi.setSatisfiedCount(2);
        mortgageApi.setPartSatisfiedCount(0);
        metricsApi.setMortgage(mortgageApi);

        when(chargesRepository.getTotalCharges(COMPANY_NUMBER)).thenReturn(30);
        when(chargesRepository.getPartSatisfiedCharges(COMPANY_NUMBER, PART_SATISFIED_STATUS)).thenReturn(20);
        when(chargesRepository.getSatisfiedAndFullSatisfiedCharges(COMPANY_NUMBER,
                SATISFIED_STATUS, FULLY_SATISFIED_STATUS)).thenReturn(10);

        chargesCountService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER, metricsApi);

        assertThat(metricsApi.getMortgage().getTotalCount()).isEqualTo(30);
        assertThat(metricsApi.getMortgage().getSatisfiedCount()).isEqualTo(10);
        assertThat(metricsApi.getMortgage().getPartSatisfiedCount()).isEqualTo(20);
    }

    @Test
    void shouldRecalculateMetricsWhenMortgageApiElementDoesNotExists() {
        MetricsApi metricsApi = new MetricsApi();
        metricsApi.setEtag(E_TAG);

        when(chargesRepository.getTotalCharges(COMPANY_NUMBER)).thenReturn(30);
        when(chargesRepository.getPartSatisfiedCharges(COMPANY_NUMBER, PART_SATISFIED_STATUS)).thenReturn(20);
        when(chargesRepository.getSatisfiedAndFullSatisfiedCharges(COMPANY_NUMBER,
                SATISFIED_STATUS, FULLY_SATISFIED_STATUS)).thenReturn(10);

        chargesCountService.recalculateMetrics(CONTEXT_ID, COMPANY_NUMBER, metricsApi);

        assertThat(metricsApi.getMortgage().getTotalCount()).isEqualTo(30);
        assertThat(metricsApi.getMortgage().getSatisfiedCount()).isEqualTo(10);
        assertThat(metricsApi.getMortgage().getPartSatisfiedCount()).isEqualTo(20);
    }
}