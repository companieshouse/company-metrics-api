package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.company.metrics.config.TestConfig;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
class CompanyMetricsReadConverterTest {

    String companyMetricsData;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws IOException {
        String inputPath = "source-bson-metrics-body-2.json";
        companyMetricsData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));
    }

    @Test
    void convert() {

        Document metricsBson = Document.parse(companyMetricsData);
        CompanyMetricsDocument companyMetricsDocument =
                objectMapper.convertValue(metricsBson, CompanyMetricsDocument.class);
        assertThat(companyMetricsDocument).isNotNull();
        assertThat(companyMetricsDocument.getCompanyMetrics()).isNotNull();
        assertThat(companyMetricsDocument.getCompanyMetrics().getMortgage().getTotalCount()).isEqualTo(51);
        assertThat(companyMetricsDocument.getCompanyMetrics().getMortgage().getSatisfiedCount()).isEqualTo(42);
        assertThat(companyMetricsDocument.getCompanyMetrics().getMortgage().getPartSatisfiedCount()).isEqualTo(0);
        assertThat(objectMapper.getRegisteredModuleIds().stream().findFirst().get().toString().contains("SimpleModule"));
    }
}