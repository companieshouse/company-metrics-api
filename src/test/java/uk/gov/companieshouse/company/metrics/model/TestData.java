package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.util.FileCopyUtils;

import uk.gov.companieshouse.api.metrics.MetricsApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class TestData {

    private ObjectMapper objectMapper;

    public MetricsApi createMetricsApi(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestdataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, MetricsApi.class);

    }

    public Updated createUpdated(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestdataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, Updated.class);

    }

    public String loadTestdataFile(String jsonFileName) throws IOException {
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
}
