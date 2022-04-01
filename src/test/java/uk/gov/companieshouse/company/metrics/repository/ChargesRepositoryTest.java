package uk.gov.companieshouse.company.metrics.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChargesRepositoryTest {

    private static final String MOCK_COMPANY_NUMBER = "12345678";


    @Mock
    ChargesRepository chargesRepository;

    @InjectMocks
    CompanyMetricsService companyMetricsService;


    @Test
    @DisplayName("When queryCompanyMetrics method is invoked with company number and status then return the count")
    void queryCompanyMetrics() throws IOException {

        when(chargesRepository.getTotalCharges(MOCK_COMPANY_NUMBER))
                .thenReturn(20);
        when(chargesRepository.getPartOrFullSatisfiedCharges(MOCK_COMPANY_NUMBER,"satisfied"))
                .thenReturn(10);
        when(chargesRepository.getPartOrFullSatisfiedCharges(MOCK_COMPANY_NUMBER,"part-satisfied"))
                .thenReturn(10);

        assertEquals(20, companyMetricsService.queryCompanyMetrics(MOCK_COMPANY_NUMBER,null));
        assertEquals(10, companyMetricsService.queryCompanyMetrics(MOCK_COMPANY_NUMBER,"satisfied"));
        assertEquals(10, companyMetricsService.queryCompanyMetrics(MOCK_COMPANY_NUMBER,"part-satisfied"));

    }

}
