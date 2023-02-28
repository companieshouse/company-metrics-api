package uk.gov.companieshouse.company.metrics.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages =
        {"uk.gov.companieshouse.company.metrics.repository.metrics"},
        mongoTemplateRef = MetricsDatabaseConfig.MONGO_TEMPLATE)
class MetricsDatabaseConfig extends MongoConfig {

    protected static final String MONGO_TEMPLATE = "metricsMongoTemplate";

    private final ApplicationContext applicationContext;

    MetricsDatabaseConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.metrics")
    MongoProperties getMetricsProps() {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = "metricsMongoTemplate")
    MongoTemplate metricsMongoTemplate() {
        MongoDatabaseFactory databaseFactory = metricsMongoDatabaseFactory();
        return new MongoTemplate(databaseFactory,
                getMongoConverter(applicationContext, databaseFactory));
    }

    @Primary
    @Bean
    MongoDatabaseFactory metricsMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(getMetricsProps().getUri());
    }
}
