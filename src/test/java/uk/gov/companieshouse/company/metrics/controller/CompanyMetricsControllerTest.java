package uk.gov.companieshouse.company.metrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CompanyMetricsController.class)
@ContextConfiguration(classes = CompanyMetricsController.class)
class CompanyMetricsControllerTest {
    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String COMPANY_URL = String.format("/company/%s/metrics", MOCK_COMPANY_NUMBER);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyMetricsService companyMetricsService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();
    }
    @Test
    @DisplayName("Retrieve company metrics for a given company number")
    void getCompanyMetrics() throws Exception {

        CompanyMetricsDocument companyMetricsDocument = testData.
                createCompanyMetricsDocument("source-metrics-body-1.json");

        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.of(companyMetricsDocument));

        mockMvc.perform(get(COMPANY_URL))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(companyMetricsDocument.getCompanyMetrics())));
        System.out.println("response = "+ objectMapper.writeValueAsString(companyMetricsDocument.getCompanyMetrics()));
    }

    @Test
    @DisplayName(
            "Given a company number with no matching company metrics return a not found response")
    void getCompanyMetricsNotFound() throws Exception {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc.perform(get(COMPANY_URL))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test()
    @DisplayName("Search for bankrupt officers - returns a 500 INTERNAL SERVER ERROR")
    void getCompanyMetricsInternalServerError() throws Exception {
        when(companyMetricsService.get(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() ->
                mockMvc.perform(get(COMPANY_URL))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().string(""))
        ).hasCause(new RuntimeException());
    }
}