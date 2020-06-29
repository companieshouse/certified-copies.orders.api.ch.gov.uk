package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.google.gson.Gson;

import java.util.Map;

public class FilingHistoryDocument {

    public FilingHistoryDocument() {}

    public FilingHistoryDocument(final String filingHistoryDate,
                                 final String filingHistoryDescription,
                                 final Map<String, Object> filingHistoryDescriptionValues,
                                 final String filingHistoryId,
                                 final String filingHistoryType) {
        this.filingHistoryDate = filingHistoryDate;
        this.filingHistoryDescription = filingHistoryDescription;
        this.filingHistoryDescriptionValues = filingHistoryDescriptionValues;
        this.filingHistoryId = filingHistoryId;
        this.filingHistoryType = filingHistoryType;
    }

    private String filingHistoryDate;

    private String filingHistoryDescription;

    private Map<String, Object> filingHistoryDescriptionValues;

    private String filingHistoryId;

    private String filingHistoryType;

    public String getFilingHistoryDate() {
        return filingHistoryDate;
    }

    public void setFilingHistoryDate(String filingHistoryDate) {
        this.filingHistoryDate = filingHistoryDate;
    }

    public String getFilingHistoryDescription() {
        return filingHistoryDescription;
    }

    public void setFilingHistoryDescription(String filingHistoryDescription) {
        this.filingHistoryDescription = filingHistoryDescription;
    }

    public Map<String, Object> getFilingHistoryDescriptionValues() {
        return filingHistoryDescriptionValues;
    }

    public void setFilingHistoryDescriptionValues(Map<String, Object> filingHistoryDescriptionValues) {
        this.filingHistoryDescriptionValues = filingHistoryDescriptionValues;
    }

    public String getFilingHistoryId() {
        return filingHistoryId;
    }

    public void setFilingHistoryId(String filingHistoryId) {
        this.filingHistoryId = filingHistoryId;
    }

    public String getFilingHistoryType() {
        return filingHistoryType;
    }

    public void setFilingHistoryType(String filingHistoryType) {
        this.filingHistoryType = filingHistoryType;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
