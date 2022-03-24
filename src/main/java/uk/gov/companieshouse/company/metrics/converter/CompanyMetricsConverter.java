package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;

@ReadingConverter
@Component
public class CompanyMetricsConverter implements Converter<Document, CompanyMetricsDocument> {

    private final ObjectMapper mapper;

    public CompanyMetricsConverter(@Qualifier("mongoConverterMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CompanyMetricsDocument convert(Document source) {
        try {
            // Use a custom converter for the ISO datetime stamps
            JsonWriterSettings writerSettings = JsonWriterSettings
                    .builder()
                    .dateTimeConverter(new JsonDateTimeConverter())
                    .build();
            return mapper.readValue(source.toJson(writerSettings), CompanyMetricsDocument.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
