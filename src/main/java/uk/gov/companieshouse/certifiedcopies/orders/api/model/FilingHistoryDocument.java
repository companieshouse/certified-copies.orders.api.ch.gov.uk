package uk.gov.companieshouse.certifiedcopies.orders.api.model;

public class FilingHistoryDocument {

    public FilingHistoryDocument() {}

    public FilingHistoryDocument(final String filingHistoryDate,
                                 final String filingHistoryDescription,
                                 final String filingHistoryId,
                                 final String filingHistoryType) {
        this.filingHistoryDate = filingHistoryDate;
        this.filingHistoryDescription = filingHistoryDescription;
        this.filingHistoryId = filingHistoryId;
        this.filingHistoryType = filingHistoryType;
    }

    private String filingHistoryDate;

    private String filingHistoryDescription;

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
}
