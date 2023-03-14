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
        {"uk.gov.companieshouse.company.metrics.repository.pscs"},
        mongoTemplateRef = PscsDatabaseConfig.MONGO_TEMPLATE)
class PscsDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "pscsMongoTemplate";

    private final ApplicationContext applicationContext;

    PscsDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.pscs")
    MongoProperties getPscsProps() {
        return new MongoProperties();
    }

    @Bean(name = "pscsMongoTemplate")
    MongoTemplate pscsMongoTemplate() {
        MongoDatabaseFactory databaseFactory = pscsMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory, getMongoConverter(applicationContext,
                databaseFactory));
    }

    @Bean
    MongoDatabaseFactory pscsMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getPscsProps().getUri());
    }
}
