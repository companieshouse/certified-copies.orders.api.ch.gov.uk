package uk.gov.companieshouse.certifiedcopies.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CollectionLocation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;

import java.util.List;

@JsonPropertyOrder(alphabetic = true)
public class CertifiedCopyItemOptionsDTO {

    @JsonProperty("collection_location")
    private CollectionLocation collectionLocation;

    @JsonProperty("contact_number")
    private String contactNumber;

    @JsonProperty("delivery_method")
    private DeliveryMethod deliveryMethod;

    @JsonProperty("delivery_timescale")
    private DeliveryTimescale deliveryTimescale;

    @JsonProperty("filing_history_documents")
    private List<FilingHistoryDocumentDTO> filingHistoryDocuments;

    @JsonProperty("forename")
    private String forename;

    @JsonProperty("surname")
    private String surname;

    public CollectionLocation getCollectionLocation() {
        return collectionLocation;
    }

    public void setCollectionLocation(CollectionLocation collectionLocation) {
        this.collectionLocation = collectionLocation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public DeliveryTimescale getDeliveryTimescale() {
        return deliveryTimescale;
    }

    public void setDeliveryTimescale(DeliveryTimescale deliveryTimescale) {
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
