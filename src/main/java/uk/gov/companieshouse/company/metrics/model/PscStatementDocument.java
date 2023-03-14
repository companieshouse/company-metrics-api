package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.model.statements.StatementApi;


@Document(collection = "#{@environment.getProperty('mongodb.psc-statements.collection.name')}")
public class PscStatementDocument {

    @Id
    private String id;
    @Field("company_number")
    private String companyNumber;

    public String getId() {
        return id;
    }

    private PscStatement data;


    public PscStatementDocument setId(String id) {
        this.id = id;
        return this;
    }

    public PscStatementDocument setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public PscStatementDocument setData(PscStatement data) {
        this.data = data;
        return this;
    }

}
