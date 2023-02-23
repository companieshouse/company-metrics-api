package uk.gov.companieshouse.company.metrics.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "#{@environment.getProperty('mongodb.appointments.collection.name')}")
public class AppointmentDocument {
    private Officer data;
    @Field("company_number")
    private String companyNumber;

    public Officer getData() {
        return data;
    }

    public AppointmentDocument setData(Officer data) {
        this.data = data;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public AppointmentDocument setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }
}
