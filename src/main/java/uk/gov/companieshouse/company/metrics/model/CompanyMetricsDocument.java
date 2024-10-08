package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.annotation.Version;
import uk.gov.companieshouse.api.metrics.MetricsApi;

public class CompanyMetricsDocument extends BaseCompanyMetricsDocument {

    @Version
    private Long version;

    public CompanyMetricsDocument() {
    }

    public CompanyMetricsDocument(MetricsApi companyMetrics, Updated updated) {
        super(companyMetrics, updated);
    }

    public Long getVersion() {
        return version;
    }

    public CompanyMetricsDocument version(Long version) {
        this.version = version;
        return this;
    }
}
