package uk.gov.companieshouse.certifiedcopies.api.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class CertifiedCopyItemData {
    @Field("id")
    private String id;

    private String companyName;

    private String companyNumber;

    private String customer_reference;

    private Integer quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCustomer_reference() {
        return customer_reference;
    }

    public void setCustomer_reference(String customer_reference) {
        this.customer_reference = customer_reference;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
