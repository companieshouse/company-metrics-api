package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;

@ExtendWith(SpringExtension.class)
public class TestData {

    @Autowired
    private ObjectMapper objectMapper;

    public MetricsApi createMetricsApi(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestDataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, MetricsApi.class);

    }

    public Updated createUpdated(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestDataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, Updated.class);

    }

    public String loadTestDataFile(String jsonFileName) throws IOException {
        InputStreamReader jsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(jsonFileName)));
        return FileCopyUtils.copyToString(jsonPayload);
    }

    public ObjectMapper getObjectMapper() {
        objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApiForCharges() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(true);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        metricsRecalculateApi.setRegisters(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApiForPscs() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(false);
        metricsRecalculateApi.setPersonsWithSignificantControl(true);
        metricsRecalculateApi.setRegisters(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApiForAppointments() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(true);
        metricsRecalculateApi.setMortgage(false);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        metricsRecalculateApi.setRegisters(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApiForRegisters() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(false);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        metricsRecalculateApi.setRegisters(true);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public MetricsRecalculateApi populateEmptyMetricsRecalculateApi() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public CompanyMetricsDocument populateCompanyMetricsDocument() throws IOException {
        MetricsApi metricsApi =
                createMetricsApi("source-metrics-body-1.json");
        Updated updated =
                createUpdated("source-metrics-updated-body-1.json");

        return new CompanyMetricsDocument(metricsApi, updated)
                .version(1L);
    }

    public CompanyMetricsDocument populateUnversionedFullCompanyMetricsDocument() throws IOException {
        String content = loadTestDataFile("full-company-metrics-document.json");
        return getObjectMapper().readValue(content, CompanyMetricsDocument.class);
    }

    public CompanyMetricsDocument populateFullCompanyMetricsDocument() throws IOException {
        return populateUnversionedFullCompanyMetricsDocument()
                .version(1L);
    }
}
