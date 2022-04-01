package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.company.metrics.config.TestConfig;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
class CompanyMetricsReadConverterTest {

    String companyMetricsData;
    private TestData testData;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws IOException {
        String inputPath = "source-bson-metrics-body-2.json";
        companyMetricsData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));
        testData = new TestData();
    }

    @Test
    void convert() throws IOException {
        Document companyMetricsBson = Document.parse(companyMetricsData);

        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsReadConverter(objectMapper).convert(companyMetricsBson);

        String expectedMetricsFileName = "expected-metrics-body-2.json";
        String expectedMetricsJson = testData.loadTestDataFile(expectedMetricsFileName);
        CompanyMetricsDocument expectedMetricsDocument = objectMapper.readValue(expectedMetricsJson, CompanyMetricsDocument.class);
        // assert that we're using the custom objectMapper
        assertThat(objectMapper.getRegisteredModuleIds().stream().findFirst().get().toString().contains("SimpleModule"));
        assertThat(companyMetricsDocument).usingRecursiveComparison().isEqualTo(expectedMetricsDocument);
    }
}