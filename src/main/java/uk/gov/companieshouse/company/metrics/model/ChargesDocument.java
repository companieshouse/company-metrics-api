package uk.gov.companieshouse.company.metrics.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.charges.ChargeApi;

@Document(collection = "#{@environment.getProperty('mongodb.charges.collection.name')}")
public class ChargesDocument {

    @Id
    private String id;

    @Field(value = "company_number")
    @Indexed(unique = true)
    private String companyNumber;

    private ChargeApi data;

    private OffsetDateTime deltaAt;

    private Updated updated;

    /**
     * default constructor.
     */
    public ChargesDocument() {
    }

    public String getId() {
        return id;
    }

    public ChargesDocument setId(String id) {
        this.id = id;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public ChargesDocument setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public ChargeApi getData() {
        return this.data;
    }

    public ChargesDocument setData(ChargeApi data) {
        this.data = data;
        return this;
    }

    public OffsetDateTime getDeltaAt() {
        return deltaAt;
    }

    public ChargesDocument setDeltaAt(OffsetDateTime deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public Updated getUpdated() {
        return updated;
    }

    public ChargesDocument setUpdated(Updated updated) {
        this.updated = updated;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChargesDocument that = (ChargesDocument) obj;
        return id.equals(that.id) && companyNumber.equals(that.companyNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyNumber);
    }
}
