package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.stream;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Implements common request payload validation.
 */
public class RequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.APPLICATION_NAMESPACE);

    /**
     * Validates the options provided, returning any errors found.
     * @param options the options to be validated
     * @param converter the converter this uses to present field names as they appear in the request JSON payload
     * @return the errors found, which will be empty if the item is found to be valid
     */
    List<String> getValidationErrors(final CertifiedCopyItemOptionsRequestDTO options, final FieldNameConverter converter) {
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
