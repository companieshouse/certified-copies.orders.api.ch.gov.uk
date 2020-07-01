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
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.Links;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.IdGeneratorService;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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

    private static final String CERTIFIED_COPIES_URL = "/orderable/certified-copies";

    private static final String CERTIFIED_COPY_ID = "CCD-123456-123456";
    private static final String UNKNOWN_CERTIFIED_COPY_ID = "CCD-000000-000000";

    private static final String COMPANY_NUMBER = "00000000";
    private static final String COMPANY_NAME = "Company Name";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String KIND = "item#certified-copy";
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String POSTAGE_COST = "0";
    private static final String DESCRIPTION = "certified copy for company 00000000";
    private static final String DESCRIPTION_IDENTIFIER = "certified-copy";

    private static final Links LINKS;

    static {
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
    private CompanyService companyService;

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

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.company_name", is(COMPANY_NAME)))
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

        assertItemSavedCorrectly(CERTIFIED_COPY_ID);
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
    @DisplayName("Successfully creates certified copy with correct descriptions")
    void createCertifiedCopyPopulatesDescription() throws Exception {
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
                .andExpect(jsonPath("$.description", is(DESCRIPTION)))
                .andExpect(jsonPath("$.description_identifier", is(DESCRIPTION_IDENTIFIER)))
                .andExpect(jsonPath("$.description_values.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.description_values."+DESCRIPTION_IDENTIFIER,
                        is(DESCRIPTION)));

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
                "item_options.filing_history_documents[0].filing_history_id: must not be null",
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
        final CertifiedCopyItemData certifiedCopyItemData = new CertifiedCopyItemData();
        certifiedCopyItemData.setCompanyName(COMPANY_NAME);
        certifiedCopyItemData.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemData.setId(CERTIFIED_COPY_ID);
        certifiedCopyItemData.setQuantity(QUANTITY);
        certifiedCopyItemData.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemData.setEtag(TOKEN_ETAG);
        certifiedCopyItemData.setLinks(LINKS);
        certifiedCopyItemData.setPostageCost(POSTAGE_COST);
        final CertifiedCopyItem newItem = new CertifiedCopyItem();
        newItem.setId(CERTIFIED_COPY_ID);
        newItem.setUserId(ERIC_IDENTITY_VALUE);
        newItem.setData(certifiedCopyItemData);
        repository.save(newItem);

        final CertifiedCopyItemResponseDTO expectedItem = new CertifiedCopyItemResponseDTO();
        expectedItem.setCompanyNumber(COMPANY_NUMBER);
        expectedItem.setCompanyName(COMPANY_NAME);
        expectedItem.setQuantity(QUANTITY);
        expectedItem.setId(CERTIFIED_COPY_ID);
        expectedItem.setCustomerReference(CUSTOMER_REFERENCE);
        expectedItem.setEtag(TOKEN_ETAG);
        expectedItem.setLinks(LINKS);
        expectedItem.setPostageCost(POSTAGE_COST);

        // When and then
        mockMvc.perform(get(CERTIFIED_COPIES_URL + "/" + CERTIFIED_COPY_ID)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItem)))
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
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
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
