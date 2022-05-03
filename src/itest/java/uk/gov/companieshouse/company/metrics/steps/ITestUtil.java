package uk.gov.companieshouse.company.metrics.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
public class ITestUtil {


    @Autowired
    private ObjectMapper objectMapper;

    public CompanyMetricsDocument populateCompanyMetricsDocument (String fileName) throws IOException {
        MetricsApi metricsApi =
                createMetricsApi("json/input/" + fileName+".json");
        Updated updated =
                createUpdated("json/input/updated.json");
        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);
        companyMetricsDocument.setId(fileName);
        return companyMetricsDocument;
    }

    public File loadJsonFile(String jsonFileName) throws IOException {
       return new ClassPathResource(jsonFileName).getFile();
    }

    public MetricsApi createMetricsApi(String jsonFileName) throws IOException {

         //return objectMapper.readValue(loadJsonFile(jsonFileName), MetricsApi.class);
         String companyMetricsDocument = loadTestDataFile(jsonFileName);
         return getObjectMapper().readValue(companyMetricsDocument, MetricsApi.class);
    }

    public Updated createUpdated(String jsonFileName) throws IOException {

        //return objectMapper.readValue(loadJsonFile(jsonFileName), Updated.class);
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

    public MetricsRecalculateApi populateMetricsRecalculateApi(Boolean mortgage) {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(mortgage);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public HttpHeaders populateHttpHeaders(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", id);
        return headers;
    }

}
