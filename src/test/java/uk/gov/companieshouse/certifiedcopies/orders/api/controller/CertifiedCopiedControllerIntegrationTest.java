package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.IdGeneratorService;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_OAUTH2_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;

@AutoConfigureMockMvc
@SpringBootTest
public class CertifiedCopiedControllerIntegrationTest {

    private static final String CERTIFIED_COPIED_URL = "/orderable/certified-copies";

    private static final String CERTIFICATE_COPY_ID = "ORD-123456-123456";

    private static final String COMPANY_NUMBER = "00000000";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String KIND = "item#certified-copy";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CertifiedCopyItemRepository repository;

    @MockBean
    private IdGeneratorService idGeneratorService;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Successfully creates certified copy item")
    void createCertifiedCopyItemSuccessfullyCreatesCertifiedCopyItem() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
                = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setContactNumber(CONTACT_NUMBER);
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.POSTAL);
        certifiedCopyItemOptionsDTORequest.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        certifiedCopyItemOptionsDTORequest.setForename(FORENAME);
        certifiedCopyItemOptionsDTORequest.setSurname(SURNAME);

        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFICATE_COPY_ID);

        mockMvc.perform(post(CERTIFIED_COPIED_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.customer_reference", is(CUSTOMER_REFERENCE)))
                .andExpect(jsonPath("$.item_options.contact_number", is(CONTACT_NUMBER)))
                .andExpect(jsonPath("$.item_options.delivery_method",
                        is(DeliveryMethod.POSTAL.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_timescale",
                        is(DeliveryTimescale.STANDARD.getJsonName())))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(FILING_HISTORY_ID)))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY)));

        assertItemSavedCorrectly(CERTIFICATE_COPY_ID);
    }

    @Test
    @DisplayName("Successfully creates certified copy with delivery as collection and same-day")
    void createCertifiedCopySetsDeliveryMethodCollectionAndDeliveryTimeScaleSameDay() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setDeliveryTimescale(DeliveryTimescale.SAME_DAY);
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.COLLECTION);
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFICATE_COPY_ID);

        mockMvc.perform(post(CERTIFIED_COPIED_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.item_options.delivery_method",
                        is(DeliveryMethod.COLLECTION.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_timescale",
                        is(DeliveryTimescale.SAME_DAY.getJsonName())))
                .andExpect(jsonPath("$.postal_delivery", is(false)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY)));

        assertItemSavedCorrectly(CERTIFICATE_COPY_ID);

    }

    @Test
    @DisplayName("Successfully creates certified copy with default delivery method and timescale")
    void createCertifiedCopyDefaultsDeliveryMethodAndDeliveryTimeScale() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFICATE_COPY_ID);

        mockMvc.perform(post(CERTIFIED_COPIED_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.item_options.delivery_method",
                        is(DeliveryMethod.POSTAL.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_timescale",
                        is(DeliveryTimescale.STANDARD.getJsonName())))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY)));

        assertItemSavedCorrectly(CERTIFICATE_COPY_ID);

    }

    @Test
    @DisplayName("Fails to create certified copy item that fails validation")
    void createCertifiedCopyItemFailsToCreateCertifiedCopyItem() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
                = new CertifiedCopyItemRequestDTO();
        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();

        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest
                .setItemOptions(certifiedCopyItemOptionsDTORequest);

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFICATE_COPY_ID);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST, asList("company_number: must not be null",
                "item_options.filing_history_documents[0].filing_history_id: must not be null",
                "quantity: must not be null"));

        mockMvc.perform(post(CERTIFIED_COPIED_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(expectedValidationError)));

        assertItemWasNotSaved(CERTIFICATE_COPY_ID);

    }



    /**
     * Verifies that the certified copy item can be retrieved
     * from the database using its expected ID value.
     * @param certifiedCopyId the expected ID of the newly created item
     */
    private void assertItemSavedCorrectly(final String certifiedCopyId) {
        final Optional<CertifiedCopyItem> retrievedCertifiedCopyItem
                = repository.findById(certifiedCopyId);
        assertThat(retrievedCertifiedCopyItem.isPresent(), is(true));
        assertThat(retrievedCertifiedCopyItem.get().getId(), is(certifiedCopyId));
    }

    /**
     * Verifies that the certified copy item cannot in fact be retrieved
     * from the database.
     * @param certifiedCopyId the expected ID of the newly created item
     */
    private void assertItemWasNotSaved(final String certifiedCopyId) {
        final Optional<CertifiedCopyItem> retrievedCertifiedCopyItem
                = repository.findById(certifiedCopyId);
        assertThat(retrievedCertifiedCopyItem.isPresent(), is(false));
    }

}
