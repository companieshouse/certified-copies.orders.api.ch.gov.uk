package uk.gov.companieshouse.certifiedcopies.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;

@JsonPropertyOrder(alphabetic = true)
public class CertifiedCopyItemDTO {

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("item_options")
    private CertifiedCopyItemOptionsDTO itemOptions;

    @JsonProperty("quantity")
    private Integer quantity;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public CertifiedCopyItemOptionsDTO getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(CertifiedCopyItemOptionsDTO itemOptions) {
        this.itemOptions = itemOptions;
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
