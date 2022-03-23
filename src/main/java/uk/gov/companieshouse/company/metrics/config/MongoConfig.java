package uk.gov.companieshouse.company.metrics.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsConverter;

@Configuration
public class MongoConfig {

    @Autowired
    private CompanyMetricsConverter companyMetricsConverter;

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(companyMetricsConverter));
    }
}
