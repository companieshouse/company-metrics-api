package uk.gov.companieshouse.company.metrics.repository.charges;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class ChargesCounts {

    public ChargesCounts() {
    }

    @Field(value = "total_count")
    private Integer totalCount;
    @Field(value = "part_satisfied")
    private Integer partSatisfied;
    @Field(value = "satisfied_or_fully_satisfied")
    private Integer satisfiedOrFullySatisfied;

    public Integer getTotalCount() {
        return totalCount;
    }

    public ChargesCounts setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public Integer getPartSatisfied() {
        return partSatisfied;
    }

    public ChargesCounts setPartSatisfied(Integer partSatisfied) {
        this.partSatisfied = partSatisfied;
        return this;
    }

    public Integer getSatisfiedOrFullySatisfied() {
        return satisfiedOrFullySatisfied;
    }

    public ChargesCounts setSatisfiedOrFullySatisfied(Integer satisfiedOrFullySatisfied) {
        this.satisfiedOrFullySatisfied = satisfiedOrFullySatisfied;
        return this;
    }
}
