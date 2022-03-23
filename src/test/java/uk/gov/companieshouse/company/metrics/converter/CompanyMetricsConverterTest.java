package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.company.metrics.config.TestConfig;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.TestData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
class CompanyMetricsConverterTest {

    String companyMetricsData;
    private TestData testData;

    @Autowired
    @Qualifier("mongoConverterMapper")
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

        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsConverter(objectMapper).convert(companyMetricsBson);

        String expectedMetricsFileName = "expected-metrics-body-2.json";
        String expectedMetricsJson = testData.loadTestdataFile(expectedMetricsFileName);
        CompanyMetricsDocument expectedMetricsDocument = objectMapper.readValue(expectedMetricsJson, CompanyMetricsDocument.class);
        // assert that we're using the custom objectMapper (mongoConverterMapper)
        assertThat(objectMapper.getDeserializationConfig().getDefaultPropertyInclusion())
                .isEqualTo(JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
        assertThat(companyMetricsDocument).usingRecursiveComparison().isEqualTo(expectedMetricsDocument);
    }
}