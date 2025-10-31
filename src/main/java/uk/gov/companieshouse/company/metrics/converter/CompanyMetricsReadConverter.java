package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.metrics.MetricsApi;

@ReadingConverter
public class CompanyMetricsReadConverter implements Converter<Document, MetricsApi> {

    private final ObjectMapper mapper;

    public CompanyMetricsReadConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MetricsApi convert(Document source) {
        try {
            // Use a custom converter for the ISO datetime stamps
            return mapper.readValue(source.toJson(), MetricsApi.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
