package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class PscStatementsCounts {

    @Field("pscs_count")
    private Integer pscsCount;
    @Field("statements_count")
    private Integer statementsCount;
    @Field("withdrawn_statements_count")
    private Integer withdrawnStatementsCount;
    @Field("active_statements_count")
    private Integer activeStatementsCount;
    @Field("ceased_pscs_count")
    private Integer ceasedPscsCount;

    public Integer getStatementsCount() {
        return statementsCount;
    }

    public PscStatementsCounts setStatementsCount(Integer statementsCount) {
        this.statementsCount = statementsCount;
        return this;
    }

    public Integer getActiveStatementsCount() {
        return activeStatementsCount;
    }

    public PscStatementsCounts setActiveStatementsCount(Integer activeStatementsCount) {
        this.activeStatementsCount = activeStatementsCount;
        return this;
    }

    public Integer getWithdrawnStatementsCount() {
        return withdrawnStatementsCount;
    }

    public PscStatementsCounts setWithdrawnStatementsCount(Integer withdrawnStatementsCount) {
        this.withdrawnStatementsCount = withdrawnStatementsCount;
        return this;
    }

    public Integer getPscsCount() {
        return pscsCount;
    }

    public PscStatementsCounts setPscsCount(Integer pscsCount) {
        this.pscsCount = pscsCount;
        return this;
    }

}
