package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "certified_copies")
public class CertifiedCopyItem {

    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private CertifiedCopyItemData data  = new CertifiedCopyItemData();

    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        data.setId(id);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CertifiedCopyItemData getData() {
        return data;
    }

    public void setData(CertifiedCopyItemData data) {
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCompanyNumber(String companyNumber) {
        data.setCompanyNumber(companyNumber);
    }

    public void setCustomerReference(String customerReference) {
        data.setCustomerReference(customerReference);
    }

    public void setQuantity(Integer quantity) {
        data.setQuantity(quantity);
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
