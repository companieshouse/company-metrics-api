package uk.gov.companieshouse.company.metrics.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.config.ApplicationConfig;
import uk.gov.companieshouse.company.metrics.model.TestData;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CompanyMetricsController.class)
@ContextConfiguration(classes = CompanyMetricsController.class)
@Import({ApplicationConfig.class})
@SuppressWarnings("unused")
class CompanyMetricsControllerTest {

    private static final String MOCK_COMPANY_NUMBER = "12345678";
    private static final String COMPANY_URL = String.format("/company/%s/metrics",
            MOCK_COMPANY_NUMBER);
    private static final String RECALCULATE_URL = String.format("/company/%s/metrics/recalculate",
            MOCK_COMPANY_NUMBER);
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";

    @MockBean
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyMetricsService companyMetricsService;

    private final TestData testData = new TestData();
    private final Gson gson = new Gson();

    @Test
    @DisplayName("Retrieve company metrics for a given company number")
    void getCompanyMetrics() throws Exception {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(
                Optional.of(testData.populateCompanyMetricsDocument()));

        mockMvc.perform(get(COMPANY_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(
                        testData.populateCompanyMetricsDocument().getCompanyMetrics())));
    }

    @Test
    @DisplayName(
            "Given a company number with no matching company metrics return a NOT FOUND response")
    void getCompanyMetricsNotFound() throws Exception {
        when(companyMetricsService.get(MOCK_COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc.perform(get(COMPANY_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test()
    @DisplayName("When calling get company metrics - returns a 500 INTERNAL SERVER ERROR")
    void getCompanyMetricsInternalServerError() {
        when(companyMetricsService.get(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() ->
                mockMvc.perform(get(COMPANY_URL)
                                .contentType(APPLICATION_JSON)
                                .header(X_REQUEST_ID, "5342342")
                                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                                .header(ERIC_IDENTITY_TYPE, "key"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().string(""))
        ).hasCause(new RuntimeException());
    }

    @Test
    @DisplayName("Post call to recalculate company charges and update metrics for a given company number")
    void postRecalculateCompanyCharges() throws Exception {

        MetricsRecalculateApi requestBody = testData.populateMetricsRecalculateApiForCharges();

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(gson.toJson(requestBody)))
                .andExpect(status().isOk());
        verify(companyMetricsService).recalculateMetrics(eq("5342342"), eq(MOCK_COMPANY_NUMBER),
                any());
    }

    @Test
    @DisplayName("Post call to recalculate company appointments and update metrics")
    void postRecalculateCompanyAppointments() throws Exception {

        MetricsRecalculateApi requestBody = testData.populateMetricsRecalculateApiForAppointments();

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(gson.toJson(requestBody)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/company/12345678/metrics"));
        verify(companyMetricsService).recalculateMetrics(eq("5342342"), eq(MOCK_COMPANY_NUMBER),
                any());
    }

    @Test
    @DisplayName("Metrics recalculation POST request fails with missing ERIC-Authorised-Key-Privilege")
    void postRecalculateCompanyChargesMissingAuthorisation() throws Exception {

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .content(gson.toJson(testData.populateMetricsRecalculateApiForCharges())))
                .andExpect(status().isForbidden());
        verify(companyMetricsService, never()).recalculateMetrics(any(), any(), any());
    }

    @Test
    @DisplayName("Metrics recalculation POST request fails with incorrect privileges")
    void postRecalculateCompanyChargesIncorrectPrivileges() throws Exception {

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "incorrect-privileges")
                        .content(gson.toJson(testData.populateMetricsRecalculateApiForCharges())))
                .andExpect(status().isForbidden());
        verify(companyMetricsService, never()).recalculateMetrics(any(), any(), any());
    }

    @Test
    @DisplayName("Metrics recalculation POST request fails with OAuth2 authorisation")
    void postRecalculateCompanyChargesIncorrectAuthorisation() throws Exception {

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .content(gson.toJson(testData.populateMetricsRecalculateApiForCharges())))
                .andExpect(status().isForbidden());
        verify(companyMetricsService, never()).recalculateMetrics(any(), any(), any());
    }

    @Test
    @DisplayName("Metrics recalculation POST request fails with OAuth2 authorisation and internal app privileges")
    void postRecalculateCompanyChargesIncorrectAuthorisationWithPrivileges() throws Exception {

        mockMvc.perform(post(RECALCULATE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(gson.toJson(testData.populateMetricsRecalculateApiForCharges())))
                .andExpect(status().isForbidden());
        verify(companyMetricsService, never()).recalculateMetrics(any(), any(), any());
    }
}