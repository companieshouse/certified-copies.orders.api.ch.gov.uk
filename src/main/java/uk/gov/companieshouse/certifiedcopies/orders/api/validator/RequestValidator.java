package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Implements common request payload validation.
 */
public class RequestValidator {
    /**
     * Validates the options provided, returning any errors found.
     * @param options the options to be validated
     * @return the errors found, which will be empty if the item is found to be valid
     */
    List<String> getValidationErrors(final CertifiedCopyItemOptionsRequestDTO options) {
        final List<String> errors = new ArrayList<>();
        if (options == null) {
            return errors;
        }
        errors.addAll(getCollectionDeliveryValidationErrors(options));

        return errors;
    }

    /**
     * Validates the collection delivery related fields on the options provided.
     * @param options the options to be validated
     * @return the resulting errors, which will be empty if the fields are found to be valid
     */
    List<String> getCollectionDeliveryValidationErrors(final CertifiedCopyItemOptionsRequestDTO options) {
        final List<String> errors = new ArrayList<>();
        if (options.getDeliveryMethod() == DeliveryMethod.COLLECTION) {
            if (isBlank(options.getForename())) {
                errors.add("forename: must not be blank when delivery method is collection");
            }
            if (isBlank(options.getSurname())) {
                errors.add("surname: must not be blank when delivery method is collection");
            }
        }
        return errors;
    }
}
