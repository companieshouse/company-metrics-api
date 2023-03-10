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
        mongoTemplateRef = PscStatementsDatabaseConfig.MONGO_TEMPLATE)
class PscStatementsDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "pscStatementsMongoTemplate";

    private final ApplicationContext applicationContext;

    PscStatementsDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.psc-statements")
    MongoProperties getPscStatementsProps() {
        return new MongoProperties();
    }

    @Bean(name = "pscStatementsMongoTemplate")
    MongoTemplate pscStatementsMongoTemplate() {
        MongoDatabaseFactory databaseFactory = pscStatementsMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory, getMongoConverter(applicationContext,
                databaseFactory));
    }

    @Bean
    MongoDatabaseFactory pscStatementsMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getPscStatementsProps().getUri());
    }
}
