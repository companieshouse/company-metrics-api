package uk.gov.companieshouse.company.metrics.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages =
        {"uk.gov.companieshouse.company.metrics.repository.appointments"},
        mongoTemplateRef = AppointmentsDatabaseConfig.MONGO_TEMPLATE)
class AppointmentsDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "appointmentsMongoTemplate";

    private final ApplicationContext applicationContext;

    AppointmentsDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.appointments")
    MongoProperties getAppointmentsProps() {
        return new MongoProperties();
    }

    @Bean(name = "appointmentsMongoTemplate")
    MongoTemplate appointmentsMongoTemplate() {
        MongoDatabaseFactory databaseFactory = appointmentsMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory, getMongoConverter(applicationContext,
                databaseFactory));
    }

    @Bean
    MongoDatabaseFactory appointmentsMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getAppointmentsProps().getUri());
    }
}
