package uk.gov.companieshouse.company.metrics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages =
        {"uk.gov.companieshouse.company.metrics.repository.appointments"},
        mongoTemplateRef = AppointmentsDatabaseConfig.MONGO_TEMPLATE
)
public class AppointmentsDatabaseConfig {

    protected static final String MONGO_TEMPLATE = "appointmentsMongoTemplate";


}
