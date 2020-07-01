package uk.gov.companieshouse.certifiedcopies.orders.api.util;

public class Error {
    private String type;
    private String error;

    public Error(String type, String error) {
        this.type = type;
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }
}
