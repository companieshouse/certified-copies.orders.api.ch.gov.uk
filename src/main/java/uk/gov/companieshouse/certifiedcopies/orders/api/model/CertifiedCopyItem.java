package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public String getCompanyNumber() {
        return data.getCompanyNumber();
    }

    public void setCustomerReference(String customerReference) {
        data.setCustomerReference(customerReference);
    }

    public void setDescription(String description) {
        data.setDescription(description);
    }

    public void setDescriptionIdentifier(String descriptionIdentifier) {
        data.setDescriptionIdentifier(descriptionIdentifier);
    }

    public void setDescriptionValues(Map<String, String> descriptionValues) {
        data.setDescriptionValues(descriptionValues);
    }

    public void setEtag(String etag) {
        data.setEtag(etag);
    }

    public void setItemCosts(List<ItemCosts> itemCosts) {
        data.setItemCosts(itemCosts);
    }

    public void setItemOptions(CertifiedCopyItemOptions itemOptions) {
        data.setItemOptions(itemOptions);
    }

    public CertifiedCopyItemOptions getItemOptions() {
        return data.getItemOptions();
    }

    public void setKind(String kind) {
        data.setKind(kind);
    }

    public void setLinks(Links links) {
        data.setLinks(links);
    }

    public void setPostageCost(String postageCost) {
        data.setPostageCost(postageCost);
    }

    public void setPostalDelivery(Boolean postalDelivery) {
        data.setPostalDelivery(postalDelivery);
    }

    public void setTotalItemCost(String totalItemCost) {
        data.setTotalItemCost(totalItemCost);
    }
    public void setQuantity(Integer quantity) {
        data.setQuantity(quantity);
    }


    @Override
    public String toString() { return new Gson().toJson(this); }
}
