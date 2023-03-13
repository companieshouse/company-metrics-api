package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.model.psc.PscApi;


@Document(collection = "#{@environment.getProperty('mongodb.pscs.collection.name')}")
public class PscDocument {

    @Id
    private String id;
    @Field("company_number")
    private String companyNumber;

    public String getId() {
        return id;
    }

    private PscApi data;


    public PscDocument setId(String id) {
        this.id = id;
        return this;
    }

}
