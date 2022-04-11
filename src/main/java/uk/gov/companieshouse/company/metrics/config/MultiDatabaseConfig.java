package uk.gov.companieshouse.company.metrics.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MultiDatabaseConfig {
    @Primary
    @Bean(name = "metricsDatabase")
    @ConfigurationProperties(prefix = "spring.data.mongodb.metrics")
    public MongoProperties getMetricsProps() {
        return new MongoProperties();
    }

    @Bean(name = "chargesDatabase")
    @ConfigurationProperties(prefix = "spring.data.mongodb.charges")
    public MongoProperties getChargesProps() {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = "metricsMongoTemplate")
    public MongoTemplate metricsMongoTemplate() {
        return new MongoTemplate(metricsMongoDatabaseFactory(getMetricsProps()));
    }

    @Bean(name = "chargesMongoTemplate")
    public MongoTemplate chargesMongoTemplate() {
        return new MongoTemplate(chargesMongoDatabaseFactory(getChargesProps()));
    }

    /**
     * metricsMongoDatabaseFactory.
     *
     * @param  mongo  mongo
     * @return  MongoDatabaseFactory
     */
    @Primary
    @Bean
    public MongoDatabaseFactory metricsMongoDatabaseFactory(MongoProperties mongo) {
        return new SimpleMongoClientDatabaseFactory(
                mongo.getUri()
        );
    }

    /**
     * chargesMongoDatabaseFactory.
     *
     * @param  mongo  mongo
     * @return  MongoDatabaseFactory
     */
    @Bean
    public MongoDatabaseFactory chargesMongoDatabaseFactory(MongoProperties mongo) {
        return new SimpleMongoClientDatabaseFactory(
                mongo.getUri()
        );
    }

}

