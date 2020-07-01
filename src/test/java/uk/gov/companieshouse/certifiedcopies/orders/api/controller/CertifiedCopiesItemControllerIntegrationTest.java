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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.*;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.IdGeneratorService;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.*;

@AutoConfigureMockMvc
@SpringBootTest
public class CertifiedCopiesItemControllerIntegrationTest {

    private static final String CERTIFIED_COPY_ID = "CCD-123456-123456";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String FILING_HISTORY_DATE = "2010-02-12";
    private static final String FILING_HISTORY_DESCRIPTION = "change-person-director-company-with-change-date";
    private static final Map<String, Object > FILING_HISTORY_DESCRIPTION_VALUES;
    private static final String FILING_HISTORY_TYPE = "CH01";
    private static final String KIND = "item#certified-copy";
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String POSTAGE_COST = "0";
    private static final Links LINKS;

    static {
        FILING_HISTORY_DESCRIPTION_VALUES = new HashMap<>();
        FILING_HISTORY_DESCRIPTION_VALUES.put("change_date", "2010-02-12");
        FILING_HISTORY_DESCRIPTION_VALUES.put("officer_name", "Thomas David Wheare");
        LINKS = new Links();
        LINKS.setSelf(CERTIFIED_COPIES_URL + "/" + CERTIFIED_COPY_ID);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CertifiedCopyItemRepository repository;

    @MockBean
    private IdGeneratorService idGeneratorService;

    @MockBean
    private FilingHistoryDocumentService filingHistoryDocumentService;

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

        final List<FilingHistoryDocument> filings =
                singletonList(new FilingHistoryDocument(FILING_HISTORY_DATE,
                                                        FILING_HISTORY_DESCRIPTION,
                                                        FILING_HISTORY_DESCRIPTION_VALUES,
                                                        FILING_HISTORY_ID,
                                                        FILING_HISTORY_TYPE));
        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
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
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(FILING_HISTORY_ID)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_type",
                        is(FILING_HISTORY_TYPE)))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY)));

        final CertifiedCopyItem retrievedCopy = assertItemSavedCorrectly(CERTIFIED_COPY_ID);
        assertFilingSavedCorrectly(retrievedCopy);
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

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
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

        assertItemSavedCorrectly(CERTIFIED_COPY_ID);

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

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
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

        assertItemSavedCorrectly(CERTIFIED_COPY_ID);

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

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST, asList("company_number: must not be null",
                "item_options.filing_history_documents[0].filing_history_id: must not be empty",
                "quantity: must not be null"));

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(expectedValidationError)));

        assertItemWasNotSaved(CERTIFIED_COPY_ID);

    }

    @Test
    @DisplayName("Successfully gets a certified copy item")
    void getCertifiedCopyItemSuccessfully() throws Exception {
        // Given
        // Create certified copy item in database
        final CertifiedCopyItem  newItem = new CertifiedCopyItem();
        newItem.setCompanyNumber(COMPANY_NUMBER);
        newItem.setId(CERTIFIED_COPY_ID);
        newItem.setQuantity(QUANTITY);
        newItem.setUserId(ERIC_IDENTITY_VALUE);
        newItem.setCustomerReference(CUSTOMER_REFERENCE);
        newItem.setEtag(TOKEN_ETAG);
        newItem.setLinks(LINKS);
        newItem.setPostageCost(POSTAGE_COST);
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(new FilingHistoryDocument(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION, FILING_HISTORY_DESCRIPTION_VALUES, FILING_HISTORY_ID, FILING_HISTORY_TYPE)));
        newItem.setItemOptions(options);
        repository.save(newItem);

        final CertifiedCopyItemResponseDTO expectedItem = new CertifiedCopyItemResponseDTO();
        expectedItem.setCompanyNumber(COMPANY_NUMBER);
        expectedItem.setQuantity(QUANTITY);
        expectedItem.setId(CERTIFIED_COPY_ID);
        expectedItem.setCustomerReference(CUSTOMER_REFERENCE);
        expectedItem.setEtag(TOKEN_ETAG);
        expectedItem.setLinks(LINKS);
        expectedItem.setPostageCost(POSTAGE_COST);
        expectedItem.setItemOptions(options);

        // When and then
        mockMvc.perform(get(CERTIFIED_COPIES_URL + "/" + CERTIFIED_COPY_ID)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItem), true))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Returns not found when a certified copy item does not exist")
    void getCertifiedCopyItemReturnsNotFound() throws Exception {
        // When and then
        mockMvc.perform(get(CERTIFIED_COPIES_URL + "/" + CERTIFIED_COPY_ID)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * Verifies that the certified copy item can be retrieved
     * from the database using its expected ID value.
     * @param certifiedCopyId the expected ID of the newly created item
     * @return the retrieved certificate item, for possible further verification
     */
    private CertifiedCopyItem assertItemSavedCorrectly(final String certifiedCopyId) {
        final Optional<CertifiedCopyItem> retrievedCertifiedCopyItem
                = repository.findById(certifiedCopyId);
        assertThat(retrievedCertifiedCopyItem.isPresent(), is(true));
        assertThat(retrievedCertifiedCopyItem.get().getId(), is(certifiedCopyId));
        return retrievedCertifiedCopyItem.get();
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

    /**
     * Verifies that the certified copy item retrieved from the database contains the single filing expected.
     * @param retrievedCopy the certified copy item retrieved from the database
     */
    private void assertFilingSavedCorrectly(final CertifiedCopyItem retrievedCopy) {
        assertThat(retrievedCopy.getData().getItemOptions().getFilingHistoryDocuments().get(0), is(notNullValue()));
        final FilingHistoryDocument retrievedFiling =
                retrievedCopy.getData().getItemOptions().getFilingHistoryDocuments().get(0);
        assertThat(retrievedFiling.getFilingHistoryDate(), is(FILING_HISTORY_DATE));
        assertThat(retrievedFiling.getFilingHistoryDescription(), is(FILING_HISTORY_DESCRIPTION));
        assertThat(retrievedFiling.getFilingHistoryId(), is(FILING_HISTORY_ID));
        assertThat(retrievedFiling.getFilingHistoryType(), is(FILING_HISTORY_TYPE));
    }

}
