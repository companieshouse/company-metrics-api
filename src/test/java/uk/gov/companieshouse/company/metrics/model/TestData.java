package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.metrics.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
public class TestData {

    @Autowired
    private ObjectMapper objectMapper;

    public MetricsApi createMetricsApi(String jsonFileName) throws IOException {

        String companyMetricsDocument = loadTestDataFile(jsonFileName);
        return getObjectMapper().readValue(companyMetricsDocument, MetricsApi.class);

    }

    public CompanyMetricsDocument populateCompanyMetricsDocument(String companyNumber) {

        CompanyMetricsDocument metricsDocument = new CompanyMetricsDocument();
        var metricsApi = new MetricsApi();
        metricsApi.setEtag("eb5f291ff2acc7b127b36802de2004c3f764c6ed");
        var mortgageApi = new MortgageApi();
        mortgageApi.setPartSatisfiedCount(1);
        mortgageApi.setSatisfiedCount(1);
        mortgageApi.setTotalCount(2);
        metricsApi.setMortgage(mortgageApi);
        var countsApi = new CountsApi();
        var appointmentsApi = new AppointmentsApi();
        appointmentsApi.setTotalCount(3);
        appointmentsApi.setActiveCount(2);
        appointmentsApi.setResignedCount(1);
        appointmentsApi.setActiveDirectorsCount(2);
        countsApi.setAppointments(appointmentsApi);
        var pscApi = new PscApi();
        pscApi.setTotalCount(2);
        pscApi.setPscsCount(3);
        countsApi.setPersonsWithSignificantControl(pscApi);
        metricsApi.setCounts(countsApi);
        var updated = new Updated();
        updated.setType("company_metrics");
        updated.setBy("header-offset-partition");
        updated.setAt(LocalDateTime.now());
        metricsDocument.setId(companyNumber);
        metricsDocument.setCompanyMetrics(metricsApi);
        metricsDocument.setUpdated(updated);

        return metricsDocument;
    }


    public Updated createUpdated(String jsonFileName) throws IOException {

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

    public MetricsRecalculateApi populateMetricsRecalculateApi() {

        MetricsRecalculateApi metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(true);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        metricsRecalculateApi.setInternalData(internalData);

        return metricsRecalculateApi;
    }

    public CompanyMetricsDocument populateCompanyMetricsDocument () throws IOException {
        MetricsApi metricsApi =
                createMetricsApi("source-metrics-body-1.json");
        Updated updated =
                createUpdated("source-metrics-updated-body-1.json");

        return new CompanyMetricsDocument(metricsApi, updated);
    }

}
