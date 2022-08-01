package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;

public interface RequestValidatable {
    CertifiedCopyItemOptions getItemOptions();
}
