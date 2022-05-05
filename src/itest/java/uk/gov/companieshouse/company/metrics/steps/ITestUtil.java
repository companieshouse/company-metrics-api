package uk.gov.companieshouse.company.metrics.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.Document;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.charges.InternalChargeApi;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
public class ITestUtil {


    @Autowired
    private ObjectMapper objectMapper;

    public CompanyMetricsDocument populateCompanyMetricsDocument (String fileName) throws IOException {
        MetricsApi metricsApi =
                createMetricsApi("json/input/" + fileName+".json");
        Updated updated =
                createUpdated("json/input/updated.json");
        CompanyMetricsDocument companyMetricsDocument = new CompanyMetricsDocument(metricsApi, updated);
        companyMetricsDocument.setId(fileName);
        return companyMetricsDocument;
    }

    public ChargesDocument populateCompanyCharges( String fileName) throws IOException {

        FileSystemResource file = new FileSystemResource("src/itest/resources/json/input/"+fileName+".json");
        return objectMapper.convertValue(readData(file), ChargesDocument.class);


       // InternalChargeApi request = createChargeAPI(fileName);
       // return createChargesDocument("companyNumber", "chargeId", request);

    }

    private Document readData(Resource resource) throws IOException {
        var data= FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                resource.getInputStream())));
        Document document = Document.parse(data);

        return document;
    }

    private InternalChargeApi createChargeAPI(String fileName) throws IOException {

        InputStreamReader jsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(fileName)));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper.readValue(jsonPayload, InternalChargeApi.class);
    }

    private ChargesDocument createChargesDocument(String companyNumber, String chargeId, InternalChargeApi requestBody) {
        OffsetDateTime at = requestBody.getInternalData().getDeltaAt();

        String by = requestBody.getInternalData().getUpdatedBy();
        var updated = new Updated();
        updated.setAt(at.toLocalDateTime());
        updated.setType("mortgage_delta");
        updated.setBy(by);

        return new ChargesDocument().setId(chargeId)
                .setCompanyNumber(companyNumber).setData(requestBody.getExternalData())
                .setUpdated(updated);
    }


    public File loadJsonFile(String jsonFileName) throws IOException {
       return new ClassPathResource(jsonFileName).getFile();
    }

    public MetricsApi createMetricsApi(String jsonFileName) throws IOException {

         //return objectMapper.readValue(loadJsonFile(jsonFileName), MetricsApi.class);
         String companyMetricsDocument = loadTestDataFile(jsonFileName);
         return getObjectMapper().readValue(companyMetricsDocument, MetricsApi.class);
    }

    public Updated createUpdated(String jsonFileName) throws IOException {

        //return objectMapper.readValue(loadJsonFile(jsonFileName), Updated.class);
          String companyMetricsDocument = loadTestDataFile(jsonFileName);
          return getObjectMapper().readValue(companyMetricsDocument, Updated.class);
    }

    public String loadTestDataFile(String jsonFileName) throws IOException {
        InputStreamReader jsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(jsonFileName)));
        return FileCopyUtils.copyToString(jsonPayload);
    }

    public ObjectMapper getObjectMapper()
    {
        objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApi(Boolean mortgage) {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(mortgage);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public HttpHeaders populateHttpHeaders(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", id);
        return headers;
    }

}
