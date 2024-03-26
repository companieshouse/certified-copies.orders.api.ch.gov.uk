package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import java.util.List;

public record ApiErrorResponsePayload(List<Error> errors) {

}
