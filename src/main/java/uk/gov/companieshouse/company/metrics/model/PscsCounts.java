package uk.gov.companieshouse.company.metrics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

public class PscsCounts {

    @Field("pscs_count")
    private Integer pscsCount;
    @Field("ceased_pscs_count")
    private Integer ceasedPscsCount;
    @Field("active_pscs_count")
    private Integer activePscsCount;

    public Integer getPscsCount() {
        return pscsCount;
    }

    public PscsCounts setPscsCount(Integer pscsCount) {
        this.pscsCount = pscsCount;
        return this;
    }

    public Integer getActivePscsCount() {
        return activePscsCount;
    }

    public PscsCounts setActivePscsCount(Integer activePscsCount) {
        this.activePscsCount = activePscsCount;
        return this;
    }

    public Integer getCeasedPscsCount() {
        return ceasedPscsCount;
    }

    public PscsCounts setCeasedPscsCount(Integer ceasedPscsCount) {
        this.ceasedPscsCount = ceasedPscsCount;
        return this;
    }
}
