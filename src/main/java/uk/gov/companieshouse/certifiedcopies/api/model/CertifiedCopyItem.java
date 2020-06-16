package uk.gov.companieshouse.certifiedcopies.api.model;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

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
}
