package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.metrics.MetricsApi;

@WritingConverter
public class CompanyMetricsWriteConverter implements
        Converter<MetricsApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyMetricsWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return CompanyMetrics BSON object.
     */
    @Override
    public BasicDBObject convert(MetricsApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
