package uk.gov.companieshouse.company.metrics.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChargesRepositoryTest {

    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String SATISFIED_STATUS="satisfied";
    private static final String FULLY_SATISFIED_STATUS="fully-satisfied";
    private static final String PART_SATISFIED_STATUS="part-satisfied";

    @Mock
    ChargesRepository chargesRepository;

    @InjectMocks
    CompanyMetricsService companyMetricsService;


    @Test
    @DisplayName("When queryCompanyMetrics method is invoked with company number and status then return the count")
    void queryCompanyMetrics() throws IOException {

        when(chargesRepository.getTotalCharges(MOCK_COMPANY_NUMBER))
                .thenReturn(20);
        when(chargesRepository.getSatisfiedAndFullSatisfiedCharges(MOCK_COMPANY_NUMBER,SATISFIED_STATUS, FULLY_SATISFIED_STATUS))
                .thenReturn(10);
        when(chargesRepository.getPartSatisfiedCharges(MOCK_COMPANY_NUMBER,PART_SATISFIED_STATUS))
                .thenReturn(10);

        when(chargesRepository.getSatisfiedAndFullSatisfiedCharges(MOCK_COMPANY_NUMBER,SATISFIED_STATUS,
                FULLY_SATISFIED_STATUS))
                .thenReturn(10);

        assertEquals(20, companyMetricsService.queryCompanyMortgages(MOCK_COMPANY_NUMBER,"none"));
        assertEquals(10, companyMetricsService.queryCompanyMortgages(MOCK_COMPANY_NUMBER,"part-satisfied"));
        assertEquals(10, companyMetricsService.queryCompanySatisfiedMortgages(MOCK_COMPANY_NUMBER,"satisfied","fully-satisfied"));

    }

}
