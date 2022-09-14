package uk.gov.companieshouse.company.metrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.company.metrics.config.ApplicationConfig;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CompanyMetricsController.class)
@ContextConfiguration(classes = CompanyMetricsController.class)
@Import({ApplicationConfig.class})
class CompanyMetricsControllerTest {
    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String COMPANY_URL = String.format("/company/%s/metrics", MOCK_COMPANY_NUMBER);
    private static final String RECALCULATE_URL = String.format("/company/%s/metrics/recalculate", MOCK_COMPANY_NUMBER);

    @MockBean
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyMetricsService companyMetricsService;

    private TestData testData;
    private Gson gson = new Gson();

    @BeforeEach
    void setUp() throws IOException {
        testData = new TestData();

    }

    @Test
    @DisplayName("Retrieve company metrics for a given company number")
    void getCompanyMetrics() throws Exception {

        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.of(testData.populateCompanyMetricsDocument()));
        mockMvc.perform(get(COMPANY_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(testData.populateCompanyMetricsDocument().getCompanyMetrics())));
    }

    @Test
    @DisplayName(
            "Given a company number with no matching company metrics return a NOT FOUND response")
    void getCompanyMetricsNotFound() throws Exception {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc.perform(get(COMPANY_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test()
    @DisplayName("When calling get company metrics - returns a 500 INTERNAL SERVER ERROR")
    void getCompanyMetricsInternalServerError() throws Exception {
        when(companyMetricsService.get(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() ->
                mockMvc.perform(get(COMPANY_URL)
                                .contentType(APPLICATION_JSON)
                                .header("x-request-id", "5342342")
                                .header("ERIC-Identity" , "SOME_IDENTITY")
                                .header("ERIC-Identity-Type", "key"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().string(""))
        ).hasCause(new RuntimeException());
    }

    @Test
    @DisplayName("Post call to recalculate company charges and update metrics for a given company number")
    void postRecalculateCompanyCharges() throws Exception {

        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.of(testData.populateCompanyMetricsDocument()));
        when(companyMetricsService.queryCompanyMortgages(MOCK_COMPANY_NUMBER, "none")).thenReturn(20);
        when(companyMetricsService.queryCompanyMortgages(MOCK_COMPANY_NUMBER, "satisfied")).thenReturn(10);
        when(companyMetricsService.queryCompanyMortgages(MOCK_COMPANY_NUMBER, "part-satisfied")).thenReturn(10);

        mockMvc.perform(post(RECALCULATE_URL)
                         .contentType(APPLICATION_JSON)
                         .header("x-request-id", "5342342")
                         .header("ERIC-Identity" , "SOME_IDENTITY")
                         .header("ERIC-Identity-Type", "key")
                         .content(gson.toJson(testData.populateMetricsRecalculateApi())))
                         .andExpect(status().isCreated());
    }

}