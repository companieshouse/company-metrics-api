package uk.gov.companieshouse.company.metrics.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.charges.ScottishAlterationsApi;
import uk.gov.companieshouse.api.charges.TransactionsLinks;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsReadConverter;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsWriteConverter;
import uk.gov.companieshouse.company.metrics.converter.EnumConverters;
import uk.gov.companieshouse.company.metrics.converter.OffsetDateTimeReadConverter;
import uk.gov.companieshouse.company.metrics.converter.OffsetDateTimeWriteConverter;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateSerializer;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateTimeDeSerializer;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateTimeSerializer;
import uk.gov.companieshouse.company.metrics.serialization.NonBlankStringSerializer;
import uk.gov.companieshouse.company.metrics.serialization.NotNullFieldObjectSerializer;
import uk.gov.companieshouse.company.metrics.serialization.OffsetDateTimeDeSerializer;
import uk.gov.companieshouse.company.metrics.serialization.OffsetDateTimeSerializer;


@Configuration
public class MongoConfig {

    /**
     * Custom mongo Conversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {

        ObjectMapper objectMapper = customMapper();
        return new MongoCustomConversions(List.of(new CompanyMetricsReadConverter(objectMapper),
                new EnumConverters.StringToEnum(),
                new EnumConverters.EnumToString(),
                new CompanyMetricsWriteConverter(objectMapper), new OffsetDateTimeReadConverter(),
                new OffsetDateTimeWriteConverter()));
    }

    /**
     * Custom object mapper with custom settings.
     */
    public ObjectMapper customMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Exclude properties with null values from being serialised
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeSerializer());
        module.addSerializer(String.class, new NonBlankStringSerializer());
        module.addSerializer(ScottishAlterationsApi.class, new NotNullFieldObjectSerializer());
        module.addSerializer(TransactionsLinks.class, new NotNullFieldObjectSerializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
