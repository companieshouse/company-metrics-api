package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.bson.Document;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public ObjectMapper getObjectMapper()
    {
        objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public List<ChargesDocument> createChargesDocumentList () throws IOException {
        List<String> chargesJsonFiles = new ArrayList<>();

        chargesJsonFiles.add(loadTestDataFile("charges-test-record-0.json"));
        chargesJsonFiles.add(loadTestDataFile("charges-test-record-1.json"));

        List<ChargesDocument> chargeList = new ArrayList<>();
        for (String document : chargesJsonFiles) {
            chargeList.add(returnChargesDocument(document));
        }
        return chargeList;
    }

    private ChargesDocument  returnChargesDocument(String record) {
        Document chargesBson = Document.parse(record);
        return getObjectMapper().convertValue(chargesBson, ChargesDocument.class);
    }

    public MetricsRecalculateApi populateMetricsRecalculateApi() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(true);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public CompanyMetricsDocument populateCompanyMetricsDocument () throws IOException {
        MetricsApi metricsApi =
                createMetricsApi("source-metrics-body-1.json");
        Updated updated =
                createUpdated("source-metrics-updated-body-1.json");

        return new CompanyMetricsDocument(metricsApi, updated);
    }

}
