package uk.gov.companieshouse.certifiedcopies.orders.api.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonMergePatch;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.ApplicationConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.controller.ApiErrors;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.PatchValidationCertifiedCopyItemDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptionsRequest;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.TestMergePatchFactory;

/**
 * Unit tests the {@link PatchItemRequestValidator} class.
 */
@SpringBootTest
@ActiveProfiles("feature-flags-disabled")
class PatchItemRequestValidatorTest {
    @Configuration
    public static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ApplicationConfig().objectMapper();
        }

        @Bean
        public Validator validator() {
            final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            return factory.getValidator();
        }

        @Bean
        public FieldNameConverter converter() {
            return new FieldNameConverter();
        }

        @Bean
        public PatchItemRequestValidator patchItemRequestValidator() {
            return new PatchItemRequestValidator(objectMapper(), validator(), converter());
        }

        @Bean
        TestMergePatchFactory patchFactory() {
            return new TestMergePatchFactory(objectMapper());
        }
    }

    private static final int TOKEN_QUANTITY = 2;
    private static final int INVALID_QUANTITY = 0;
    private static final String TOKEN_STRING = "TOKEN VALUE";
    static final Map<String, String> TOKEN_VALUES = new HashMap<>();
    private static final ItemCosts TOKEN_ITEM_COSTS = new ItemCosts();
    private static final boolean TOKEN_POSTAL_DELIVERY_VALUE = true;

    @Autowired
    private PatchItemRequestValidator validatorUnderTest;

    @Autowired
    private TestMergePatchFactory patchFactory;

    private PatchValidationCertifiedCopyItemDTO itemUpdate;

    private CertifiedCopyItemOptions certifiedCopyItemOptions;

    @MockBean
    private RequestValidatable requestValidatable;

    @BeforeEach
    void setUp() {
        itemUpdate = new PatchValidationCertifiedCopyItemDTO();
        certifiedCopyItemOptions = new CertifiedCopyItemOptions();
        when(requestValidatable.getItemOptions()).thenReturn(certifiedCopyItemOptions);
    }

    @Test
    @DisplayName("No errors")
    void getValidationErrorsReturnsNoErrors() throws IOException {
        // Given
        CertifiedCopyItemOptionsRequest itemOptionsRequest = new CertifiedCopyItemOptionsRequest();

        itemOptionsRequest.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        itemUpdate.setQuantity(TOKEN_QUANTITY);
        itemUpdate.setItemOptions(itemOptionsRequest);
        itemUpdate.setCompanyNumber("00000001");
        itemUpdate.setCustomerReference("ch-0001");
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, is(empty()));
        assertThat(itemOptionsRequest.getDeliveryTimescale(), is(DeliveryTimescale.STANDARD));
    }

    @Test
    @DisplayName("Quantity must be greater than 0")
    void getValidationErrorsRejectsZeroQuantity() throws IOException {
        // Given
        itemUpdate.setQuantity(INVALID_QUANTITY);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, contains(ApiErrors.raiseError(ApiErrors.ERR_QUANTITY_AMOUNT, "quantity: must be greater than or equal to 1")));
    }

    @Test
    @DisplayName("Validation error raised if unknown field specified")
    void getValidationErrorsRaisesErrorIfUnknownFieldSpecified() throws IOException {
        // Given
        final String jsonWithUnknownField = "{ \"idx\": \"CHS1\" }";
        final JsonMergePatch patch = patchFactory.patchFromJson(jsonWithUnknownField);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, contains(ApiErrors.ERR_JSON_PROCESSING));
    }

    /**
     * Utility method that asserts that the validator produces a "<field name>: must be null"
     * error message.
     *
     * @throws IOException should something unexpected happen
     */
    private void assertFieldMustBeNullErrorProduced(ApiError apiError) throws IOException {
        // Given
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);
        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);
        // Then
        assertThat(errors, contains(apiError));
    }
}
