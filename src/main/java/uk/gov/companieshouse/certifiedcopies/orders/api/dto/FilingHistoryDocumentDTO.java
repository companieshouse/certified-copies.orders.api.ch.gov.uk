package uk.gov.companieshouse.certifiedcopies.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilingHistoryDocumentDTO {

    @JsonProperty("filing_history_id")
    private String filingHistoryId;

    public String getFilingHistoryId() {
        return filingHistoryId;
    }

    public void setFilingHistoryId(String filingHistoryId) {
        this.filingHistoryId = filingHistoryId;
    }
}
