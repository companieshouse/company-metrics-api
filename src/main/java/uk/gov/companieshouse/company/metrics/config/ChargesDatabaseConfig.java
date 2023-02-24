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
        {"uk.gov.companieshouse.company.metrics.repository.charges"},
        mongoTemplateRef = ChargesDatabaseConfig.MONGO_TEMPLATE)
public class ChargesDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "chargesMongoTemplate";

    private final ApplicationContext applicationContext;

    public ChargesDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.charges")
    MongoProperties getChargesProps() {
        return new MongoProperties();
    }

    @Bean(name = "chargesMongoTemplate")
    MongoTemplate chargesMongoTemplate() {
        MongoDatabaseFactory databaseFactory = chargesMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory,
                getMongoConverter(applicationContext, databaseFactory));
    }

    @Bean
    MongoDatabaseFactory chargesMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getChargesProps().getUri());
    }
}
