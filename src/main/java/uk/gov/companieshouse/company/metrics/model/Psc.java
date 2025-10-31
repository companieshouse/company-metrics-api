package uk.gov.companieshouse.company.metrics.model;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Field;


public class Psc {

    @Field("ceased_on")
    private Instant ceasedOn;

    public Instant getCeasedOn() {
        return ceasedOn;
    }

    public Psc setCeasedOn(Instant ceasedOn) {
        this.ceasedOn = ceasedOn;
        return this;
    }
}
