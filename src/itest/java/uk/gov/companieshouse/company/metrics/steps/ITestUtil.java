package uk.gov.companieshouse.company.metrics.steps;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.charges.ChargeApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.InternalData;
import uk.gov.companieshouse.company.metrics.model.ChargesDocument;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
public class ITestUtil {

    public ChargesDocument createChargesDocumentWithoutData(String companyNumber,
        String chargeId) {

        var updated = new Updated();
        updated.setAt(LocalDateTime.now());
        updated.setType("mortgage_delta");
        updated.setBy("updatedBy");
        return new ChargesDocument()
            .setId(chargeId)
            .setCompanyNumber(companyNumber)
            .setData(null)
            .setUpdated(updated);
    }

    public ChargesDocument createChargesDocument(String companyNumber,
                                                 String chargeId,
                                                 ChargeApi.StatusEnum status) {

        var updated = new Updated();
        updated.setAt(LocalDateTime.now());
        updated.setType("mortgage_delta");
        updated.setBy("updatedBy");
        ChargeApi chargeApi = new ChargeApi();
        chargeApi.setId(chargeId);
        chargeApi.setChargeCode("12");
        chargeApi.setStatus(status);
        return new ChargesDocument()
                    .setId(chargeId)
                    .setCompanyNumber(companyNumber)
                    .setData(chargeApi)
                    .setUpdated(updated);
    }

    public CompanyMetricsDocument createTestCompanyMetricsDocument(String companyNumber) {

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
        appointmentsApi.setTotalCount(17);
        appointmentsApi.setActiveCount(1);
        appointmentsApi.setResignedCount(16);
        appointmentsApi.setActiveDirectorsCount(2);
        appointmentsApi.setActiveSecretariesCount(2);
        appointmentsApi.setActiveLlpMembersCount(0);

        countsApi.setAppointments(appointmentsApi);
        metricsApi.setCounts(countsApi);

        var updated = new Updated();
        updated.setType("company_metrics");
        updated.setBy("partition-offset-id");
        updated.setAt(LocalDateTime.now());
        metricsDocument.setId(companyNumber);
        metricsDocument.setCompanyMetrics(metricsApi);
        metricsDocument.setUpdated(updated);

        return metricsDocument;
    }

    public CompanyMetricsDocument createTestCompanyMetricsDocumentWithoutData(String companyNumber) {

        CompanyMetricsDocument metricsDocument = new CompanyMetricsDocument();
        var updated = new Updated();
        updated.setType("company_metrics");
        updated.setBy("partition-offset-id");
        updated.setAt(LocalDateTime.now());
        metricsDocument.setId(companyNumber);
        metricsDocument.setCompanyMetrics(null);
        metricsDocument.setUpdated(updated);

        return metricsDocument;
    }

    public MetricsRecalculateApi populateMetricsRecalculateApi(Boolean mortgage) {

        var metricsRecalculateApi = new MetricsRecalculateApi();
        metricsRecalculateApi.setAppointments(false);
        metricsRecalculateApi.setMortgage(mortgage);
        metricsRecalculateApi.setPersonsWithSignificantControl(false);
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("updatedBy");
        internalData.setUpdatedAt(OffsetDateTime.now());
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
