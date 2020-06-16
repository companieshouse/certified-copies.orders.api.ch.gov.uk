package uk.gov.companieshouse.certifiedcopies.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;

@JsonPropertyOrder(alphabetic = true)
public class CertifiedCopyItemDTO {
    private String company_number;
    private String customer_reference;
    private Integer quantity;

    public String getCompany_number() {
        return company_number;
    }

    public void setCompany_number(String company_number) {
        this.company_number = company_number;
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

    @Override
    public String toString() { return new Gson().toJson(this); }
}
