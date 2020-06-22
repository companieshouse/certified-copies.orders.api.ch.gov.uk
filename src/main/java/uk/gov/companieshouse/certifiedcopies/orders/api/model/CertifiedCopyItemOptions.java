package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import java.util.List;

public class CertifiedCopyItemOptions {

    private CollectionLocation collectionLocation;

    private String contactNumber;

    private DeliveryMethod deliveryMethod;

    private DeliveryTimescale deliveryTimescale;

    private List<FilingHistoryDocument> filingHistoryDocuments;

    private String forename;

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

    public List<FilingHistoryDocument> getFilingHistoryDocuments() {
        return filingHistoryDocuments;
    }

    public void setFilingHistoryDocuments(List<FilingHistoryDocument> filingHistoryDocuments) {
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
