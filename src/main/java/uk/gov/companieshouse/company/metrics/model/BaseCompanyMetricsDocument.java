package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.metrics.MetricsApi;

@Document(collection = "#{@environment.getProperty('mongodb.company-metrics.collection.name')}")
public class BaseCompanyMetricsDocument {

    @Id
    @Field("_id")
    private String id;

    @Field("data")
    @JsonProperty("data")
    private MetricsApi companyMetrics;

    private Updated updated;

    /**
     * Constructor taking in MetricsApi and Updated objects.
     */
    protected BaseCompanyMetricsDocument(MetricsApi companyMetrics, Updated updated) {
        this.companyMetrics = companyMetrics;
        this.updated = updated;
    }

    /**
     * Constructor.
     */
    protected BaseCompanyMetricsDocument() {
        this.companyMetrics = null;
        this.updated = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetricsApi getCompanyMetrics() {
        return companyMetrics;
    }

    /**
     * setter for companyMetrics.
     */
    public MetricsApi setCompanyMetrics(MetricsApi companyMetrics) {

        this.companyMetrics = companyMetrics;
        return this.companyMetrics;
    }

    public Updated getUpdated() {
        return updated;
    }

    public Updated setUpdated(Updated updated) {
        this.updated = updated;
        return this.updated;
    }
}