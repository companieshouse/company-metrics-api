package uk.gov.companieshouse.company.metrics.model;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Field;

public class Officer {

    @Field("officer_role")
    private String officerRole;

    @Field("resigned_on")
    private Instant resignedOn;

    public String getOfficerRole() {
        return officerRole;
    }

    public Officer setOfficerRole(String officerRole) {
        this.officerRole = officerRole;
        return this;
    }

    public Instant getResignedOn() {
        return resignedOn;
    }

    public Officer setResignedOn(Instant resignedOn) {
        this.resignedOn = resignedOn;
        return this;
    }
}
