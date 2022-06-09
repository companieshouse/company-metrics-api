package uk.gov.companieshouse.company.metrics.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;


@Configuration
@Import(MongoConfig.class)
public class MultiDatabaseConfig {

    @Autowired
    private MongoCustomConversions mongoCustomConversions;

    @Autowired
    private ApplicationContext applicationContext;

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

    /**
     * Create metricsMongoTemplate.
     * @return MongoTemplate
     */
    @Primary
    @Bean(name = "metricsMongoTemplate")
    public MongoTemplate metricsMongoTemplate() {
        MongoDatabaseFactory metricsMongoDatabaseFactory =
                metricsMongoDatabaseFactory(getMetricsProps());
        return new MongoTemplate(metricsMongoDatabaseFactory,
                getMongoConverter(metricsMongoDatabaseFactory));
    }

    /**
     * Create chargesMongoTemplate.
     * @return MongoTemplate
     */
    @Bean(name = "chargesMongoTemplate")
    public MongoTemplate chargesMongoTemplate() {
        MongoDatabaseFactory chargesMongoDatabaseFactory =
                chargesMongoDatabaseFactory(getChargesProps());
        return new MongoTemplate(chargesMongoDatabaseFactory,
                getMongoConverter(chargesMongoDatabaseFactory));
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

    /**
     * This method takes in custome conversions and created MongoConverter,
     * so that MongoConverter can be passed onto MongoTemplate.
     * @param factory MongoDatabaseFactory
     * @return MongoConverter
     */
    private MongoConverter getMongoConverter(MongoDatabaseFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(mongoCustomConversions.getSimpleTypeHolder());
        mappingContext.setApplicationContext(applicationContext);
        mappingContext.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(mongoCustomConversions);
        converter.afterPropertiesSet();
        return converter;
    }
}

