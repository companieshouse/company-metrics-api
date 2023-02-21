package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class AppointmentsCounts {

    @Field("total_count")
    private Integer totalCount;
    @Field("active_directors_count")
    private Integer activeDirectorsCount;
    @Field("resigned_count")
    private Integer resignedCount;
    @Field("active_secretaries_count")
    private Integer activeSecretariesCount;
    @Field("active_llp_members_count")
    private Integer activeLlpMembersCount;
    @Field("active_count")
    private Integer activeCount;

    public Integer getTotalCount() {
        return totalCount;
    }

    public AppointmentsCounts setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public Integer getActiveDirectorsCount() {
        return activeDirectorsCount;
    }

    public AppointmentsCounts setActiveDirectorsCount(Integer activeDirectorsCount) {
        this.activeDirectorsCount = activeDirectorsCount;
        return this;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public AppointmentsCounts setResignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }

    public Integer getActiveSecretariesCount() {
        return activeSecretariesCount;
    }

    public AppointmentsCounts setActiveSecretariesCount(Integer activeSecretariesCount) {
        this.activeSecretariesCount = activeSecretariesCount;
        return this;
    }

    public Integer getActiveLlpMembersCount() {
        return activeLlpMembersCount;
    }

    public AppointmentsCounts setActiveLlpMembersCount(Integer activeLlpMembersCount) {
        this.activeLlpMembersCount = activeLlpMembersCount;
        return this;
    }

    public Integer getActiveCount() {
        return activeCount;
    }

    public AppointmentsCounts setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
        return this;
    }
}
