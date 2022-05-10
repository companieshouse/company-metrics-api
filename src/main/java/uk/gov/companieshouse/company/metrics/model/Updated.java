package uk.gov.companieshouse.company.metrics.model;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

public class Updated {

    @Field("at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime at;

    private String by;

    private String type;

    /**
     * Instantiate company insolvency updated data.
     * @param at timestamp for the delta change
     * @param by updated by
     * @param type the delta type
     */
    public Updated(LocalDateTime at, String by, String type) {
        this.at = at;
        this.by = by;
        this.type = type;
    }

    /**
     * Default Constructor.
     */
    public Updated() {
        this.at = null;
        this.by = null;
        this.type = null;
    }

    public LocalDateTime getAt() {
        return at;
    }

    public void setAt(LocalDateTime at) {
        this.at = at;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Updated{");
        sb.append("at=").append(at);
        sb.append(", type=").append(type);
        sb.append(", by=").append(by);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Updated updated = (Updated) obj;
        return at.equals(updated.at) && type.equals(updated.type) && by.equals(updated.by);
    }

    @Override
    public int hashCode() {
        return Objects.hash(at, type, by);
    }
}
