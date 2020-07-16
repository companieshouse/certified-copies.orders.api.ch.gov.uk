package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod.COLLECTION;

/**
 * Unit tests the {@link CreateItemRequestValidator} class.
 */
class CreateItemRequestValidatorTest {

    private CreateItemRequestValidator validatorUnderTest;

    @BeforeEach
    void setUp() {
        validatorUnderTest = new CreateItemRequestValidator(new FieldNameConverter());
    }

    @Test
    @DisplayName("Collection details are mandatory for collection delivery method")
    void collectionDetailsAreMandatoryForCollectionDeliveryMethod() {
        // Given
        final CertifiedCopyItemRequestDTO item = new CertifiedCopyItemRequestDTO();
        final CertifiedCopyItemOptionsRequestDTO options = new CertifiedCopyItemOptionsRequestDTO();
        options.setDeliveryMethod(COLLECTION);
        item.setItemOptions(options);

        // When
        final List<String> errors = validatorUnderTest.getValidationErrors(item);

        // Then
        assertThat(errors, containsInAnyOrder(
                "forename: must not be blank when delivery method is collection",
                "surname: must not be blank when delivery method is collection"));
    }
}
