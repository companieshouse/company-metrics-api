package uk.gov.companieshouse.company.metrics.model;

public class UnversionedCompanyMetricsDocument extends BaseCompanyMetricsDocument {

    private Long version;

    UnversionedCompanyMetricsDocument() {
    }

    /**
     * Copy constructor.
     *
     * @param copy Document to copy
     */
    public UnversionedCompanyMetricsDocument(CompanyMetricsDocument copy) {
        this.setId(copy.getId());
        this.setCompanyMetrics(copy.getCompanyMetrics());
        this.setUpdated(copy.getUpdated());
        this.version = 0L;
    }

    public Long getVersion() {
        return version;
    }

    public UnversionedCompanyMetricsDocument version(Long version) {
        this.version = version;
        return this;
    }
}
