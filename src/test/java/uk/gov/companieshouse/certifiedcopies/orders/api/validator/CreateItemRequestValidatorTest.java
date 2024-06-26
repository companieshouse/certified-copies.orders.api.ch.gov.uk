package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod.COLLECTION;

/**
 * Unit tests the {@link CreateCertifiedCopyItemRequestValidator} class.
 */
class CreateItemRequestValidatorTest {

    private CreateCertifiedCopyItemRequestValidator validatorUnderTest;

    @BeforeEach
    void setUp() {
        validatorUnderTest = new CreateCertifiedCopyItemRequestValidator();
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
