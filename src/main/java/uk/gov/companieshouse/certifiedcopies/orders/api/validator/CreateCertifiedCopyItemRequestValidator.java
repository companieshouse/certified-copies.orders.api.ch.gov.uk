package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements validation of the request payload specific to the create item request only.
 */
@Component
public class CreateCertifiedCopyItemRequestValidator extends RequestValidator {

    private final FieldNameConverter converter;

    /**
     * Constructor.
     * @param converter the converter this uses to present field names as they appear in the request JSON payload
     */
    public CreateCertifiedCopyItemRequestValidator(FieldNameConverter converter) {
        this.converter = converter;
    }

    /**
     * Validates the item provided, returning any errors found.
     * @param item the item to be validated
     * @return the errors found, which will be empty if the item is found to be valid
     */
    public List<String> getValidationErrors(final CertifiedCopyItemRequestDTO item) {
        final List<String> errors = new ArrayList<>();
        final CertifiedCopyItemOptionsRequestDTO options = item.getItemOptions();
        errors.addAll(getValidationErrors(options, converter));
        return errors;
    }

}
