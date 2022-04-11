package uk.gov.companieshouse.company.metrics.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages =
        {"uk.gov.companieshouse.company.metrics.repository.charges"},
        mongoTemplateRef = ChargesDatabaseConfig.MONGO_TEMPLATE
)
public class ChargesDatabaseConfig {
    
    protected static final String MONGO_TEMPLATE = "chargesMongoTemplate";
}
