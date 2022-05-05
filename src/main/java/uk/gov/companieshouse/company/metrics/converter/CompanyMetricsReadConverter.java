package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import uk.gov.companieshouse.api.charges.ChargeApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;

@ReadingConverter
public class CompanyMetricsReadConverter implements Converter<Document, CompanyMetricsDocument> {

    private final ObjectMapper mapper;

    public CompanyMetricsReadConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CompanyMetricsDocument convert(Document source) {
        try {
            // Use a custom converter for the ISO datetime stamps
            /*JsonWriterSettings writerSettings = JsonWriterSettings
                    .builder()
                    .dateTimeConverter(new JsonDateTimeConverter())
                    .build();
            CompanyMetricsDocument companyMetricsDocument = mapper
                    .readValue(source.toJson(writerSettings), CompanyMetricsDocument.class);
            return companyMetricsDocument;*/

            return mapper.readValue(source.toJson(), CompanyMetricsDocument.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
