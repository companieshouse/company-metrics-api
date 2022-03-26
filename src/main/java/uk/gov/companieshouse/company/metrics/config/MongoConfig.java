package uk.gov.companieshouse.company.metrics.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsReadConverter;
import uk.gov.companieshouse.company.metrics.converter.CompanyMetricsWriteConverter;
import uk.gov.companieshouse.company.metrics.converter.EnumConverters;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.company.metrics.serialization.LocalDateSerializer;

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
                new CompanyMetricsWriteConverter(objectMapper)));
    }

    /**
     * Custom object mapper with custom settings.
     */
    public ObjectMapper customMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Exclude properties with null values from being serialised
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
