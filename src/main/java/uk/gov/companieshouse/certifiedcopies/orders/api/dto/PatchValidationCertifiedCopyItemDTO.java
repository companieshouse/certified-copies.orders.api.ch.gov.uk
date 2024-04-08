package uk.gov.companieshouse.certifiedcopies.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import jakarta.validation.constraints.Min;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptionsRequest;

/**
 * Instantiated from PATCH request JSON body to facilitate PATCH request validation.
 */
public class PatchValidationCertifiedCopyItemDTO {

    @JsonProperty("item_options")
    private CertifiedCopyItemOptionsRequest itemOptions;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("customer_reference")
    private String customerReference;

    @Min(1)
    @JsonProperty("quantity")
    private Integer quantity;

    public void setItemOptions(
            CertifiedCopyItemOptionsRequest itemOptions) {
        this.itemOptions = itemOptions;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
