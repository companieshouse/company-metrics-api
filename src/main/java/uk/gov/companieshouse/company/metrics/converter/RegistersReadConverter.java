package uk.gov.companieshouse.company.metrics.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.registers.CompanyRegister;

@ReadingConverter
public class RegistersReadConverter implements Converter<Document, CompanyRegister> {

    private final ObjectMapper mapper;

    public RegistersReadConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CompanyRegister convert(Document source) {
        try {
            // Use a custom converter for the ISO datetime stamps
            return mapper.readValue(source.toJson(), CompanyRegister.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
