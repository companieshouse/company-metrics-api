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
        {"uk.gov.companieshouse.company.metrics.repository.registers"},
        mongoTemplateRef = RegistersDatabaseConfig.MONGO_TEMPLATE)
public class RegistersDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "registersMongoTemplate";

    private final ApplicationContext applicationContext;

    public RegistersDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.registers")
    MongoProperties getRegistersProps() {
        return new MongoProperties();
    }

    @Bean(name = "registersMongoTemplate")
    MongoTemplate registersMongoTemplate() {
        MongoDatabaseFactory databaseFactory = registersMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory,
                getMongoConverter(applicationContext, databaseFactory));
    }

    @Bean
    MongoDatabaseFactory registersMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getRegistersProps().getUri());
    }
}
