package uk.gov.companieshouse.company.metrics.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.company.metrics.converter.ChargeApiWriteConverter;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsReadConverter;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsWriteConverter;
import uk.gov.companieshouse.company.metrics.converter.EnumConverters;
import uk.gov.companieshouse.company.metrics.serialization.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@TestConfiguration
public class TestConfig {


    /**
     * Custom object mapper with custom settings.
     */
    /*@Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Exclude properties with null values from being serialised
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeSerializer());
        objectMapper.registerModule(module);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }*/


    /**
     * Custom mongo Conversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {

        ObjectMapper objectMapper = objectMapper();
        return new MongoCustomConversions(List.of(new CompanyMetricsReadConverter(objectMapper),
                new EnumConverters.StringToEnum(),
                new EnumConverters.EnumToString(),
                new CompanyMetricsWriteConverter(objectMapper)));
    }

    /**
     * Custom object mapper with custom settings.
     */

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Exclude properties with null values from being serialised
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeSerializer());
        objectMapper.registerModule(module);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
