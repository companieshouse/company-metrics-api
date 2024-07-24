package uk.gov.companieshouse.company.metrics.model;

import java.util.Objects;
import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.registers.CompanyRegister;

@Document(collection = "#{@environment.getProperty('mongodb.registers.collection.name')}")
public class RegistersDocument {

    @Id
    private String id;

    private Created created;

    @Field("data")
    private CompanyRegister data;

    @Field("delta_at")
    private String deltaAt;

    private Updated updated;

    public String getId() {
        return id;
    }

    public RegistersDocument setId(String id) {
        this.id = id;
        return this;
    }

    public Created getCreated() {
        return created;
    }

    public RegistersDocument setCreated(Created created) {
        this.created = created;
        return this;
    }

    public CompanyRegister getData() {
        return data;
    }

    public RegistersDocument setData(CompanyRegister data) {
        this.data = data;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public RegistersDocument setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public Updated getUpdated() {
        return updated;
    }

    public RegistersDocument setUpdated(Updated updated) {
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
        RegistersDocument document = (RegistersDocument) obj;
        return Objects.equals(id, document.id) && Objects.equals(created, document.created)
                && Objects.equals(data, document.data) && Objects.equals(deltaAt, document.deltaAt)
                && Objects.equals(updated, document.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created, data, deltaAt, updated);
    }
}
