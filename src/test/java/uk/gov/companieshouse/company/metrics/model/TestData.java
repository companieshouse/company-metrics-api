package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.charges.ChargesApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class TestData {

    private ObjectMapper objectMapper;

    public CompanyMetricsDocument createCompanyMetricsDocument(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestdataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, CompanyMetricsDocument.class);

    }

    public String loadTestdataFile(String jsonFileName) throws IOException {
        InputStreamReader exampleChargesJsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(jsonFileName)));
        return FileCopyUtils.copyToString(exampleChargesJsonPayload);
    }

    private ObjectMapper getObjectMapper()
    {
        objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
