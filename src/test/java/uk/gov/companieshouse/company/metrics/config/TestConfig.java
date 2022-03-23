package uk.gov.companieshouse.company.metrics.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@TestConfiguration
public class TestConfig {

    @Bean("mongoConverterMapper")
    public ObjectMapper customMapper() {
        ObjectMapper customMapper = new Jackson2ObjectMapperBuilder().build();
        // Exclude properties with null values from being serialised
        customMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return customMapper;
    }

}
