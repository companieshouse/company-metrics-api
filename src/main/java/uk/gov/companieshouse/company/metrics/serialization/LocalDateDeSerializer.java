package uk.gov.companieshouse.company.metrics.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import uk.gov.companieshouse.company.metrics.util.DateTimeFormatter;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        try {
            var dateJsonNode = jsonNode.get("$date");
            if (dateJsonNode == null) {
                return DateTimeFormatter.parse(jsonNode.textValue());
            } else if (dateJsonNode.getNodeType() == JsonNodeType.STRING) {
                var dateStr = dateJsonNode.textValue();
                return DateTimeFormatter.parse(dateStr);
            } else {
                var longDate = dateJsonNode.get("$numberLong").asLong();
                var dateStr = Instant.ofEpochMilli(new Date(longDate).getTime()).toString();
                return DateTimeFormatter.parse(dateStr);
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Failed while deserializing "
                    + "date value for json node: %s", jsonNode), ex);
        }
    }
}
