package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import java.util.List;

public class CertifiedCopyItemOptions {

    private String collectionLocation;

    private String contactNumber;

    private String deliveryMethod;

    private String deliveryTimescale;

    private List<FilingHistoryDocument> filingHistoryDocuments;

    private String forename;

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
