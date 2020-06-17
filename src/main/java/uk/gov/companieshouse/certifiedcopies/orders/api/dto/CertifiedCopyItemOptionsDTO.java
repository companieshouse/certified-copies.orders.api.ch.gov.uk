package uk.gov.companieshouse.certifiedcopies.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CertifiedCopyItemOptionsDTO {

    @JsonProperty("collection_location")
    private String collectionLocation;

    @JsonProperty("contact_number")
    private String contactNumber;

    @JsonProperty("delivery_method")
    private String deliveryMethod;

    @JsonProperty("delivery_timescale")
    private String deliveryTimescale;

    @JsonProperty("filing_history_documents")
    private List<FilingHistoryDocumentDTO> filingHistoryDocuments;

    @JsonProperty("forename")
    private String forename;

    @JsonProperty("surname")
    private String surname;

    public String getCollectionLocation() {
        return collectionLocation;
    }

    public void setCollectionLocation(String collectionLocation) {
        this.collectionLocation = collectionLocation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getDeliveryTimescale() {
        return deliveryTimescale;
    }

    public void setDeliveryTimescale(String deliveryTimescale) {
        this.deliveryTimescale = deliveryTimescale;
    }

    public List<FilingHistoryDocumentDTO> getFilingHistoryDocuments() {
        return filingHistoryDocuments;
    }

    public void setFilingHistoryDocuments(List<FilingHistoryDocumentDTO> filingHistoryDocuments) {
        this.filingHistoryDocuments = filingHistoryDocuments;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
