package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "#{@environment.getProperty('mongodb.pscs.collection.name')}")
public class PscDocument {

    @Id
    private String id;
    @Field("company_number")
    private String companyNumber;

    public String getId() {
        return id;
    }

    private Psc data;


    public PscDocument setId(String id) {
        this.id = id;
        return this;
    }

    public PscDocument setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public PscDocument setData(Psc data) {
        this.data = data;
        return this;
    }

}
