package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import java.util.List;

public class ApiErrorResponsePayload {

    private List<Error> errors;

    public ApiErrorResponsePayload(List<Error> errors) {
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }
}
