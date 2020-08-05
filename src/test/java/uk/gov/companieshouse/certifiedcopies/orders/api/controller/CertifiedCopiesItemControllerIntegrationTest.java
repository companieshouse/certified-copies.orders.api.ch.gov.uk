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
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.Links;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.ApiClientService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.DescriptionProviderService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.IdGeneratorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_INCORPORATION;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPIES_URL;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_NEW_INCORPORATION_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_OAUTH2_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.FILING_HISTORY_TYPE_CH01;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.FILING_HISTORY_TYPE_NEWINC;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;

@AutoConfigureMockMvc
@SpringBootTest
class CertifiedCopiesItemControllerIntegrationTest {

    private static final String CERTIFIED_COPY_ID = "CCD-123456-123456";

    private static final String COMPANY_NUMBER = "00000000";
    private static final String COMPANY_NAME = "Company Name";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY_1 = 1;
    private static final int QUANTITY_3 = 3;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String FILING_HISTORY_DATE = "2010-02-12";
    private static final String FILING_HISTORY_DESCRIPTION = "change-person-director-company-with-change-date";
    private static final Map<String, Object> FILING_HISTORY_DESCRIPTION_VALUES;
    private static final String FILING_HISTORY_ID_01 = "01";
    private static final String FILING_HISTORY_ID_02 = "02";
    private static final String KIND = "item#certified-copy";
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String POSTAGE_COST = "0";
    private static final String DESCRIPTION = "certified copy for company 00000000";
    private static final String DESCRIPTION_IDENTIFIER = "certified-copy";
    private static final String TOTAL_ITEM_COST = "15";
    private static final String TOTAL_ITEM_COST_SAME_DAY = "50";
    private static final String TOTAL_ITEM_COST_NEWINC = "30";
    private static final String TOTAL_ITEM_COST_MULTI = "45";
    private static final String TOTAL_ITEM_COST_MULTI_QUANTITY_3 = "135";
    private static final String DISCOUNT = "0";

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
    private CompanyService companyService;

    @MockBean
    private FilingHistoryDocumentService filingHistoryDocumentService;

    @MockBean
    private DescriptionProviderService descriptionProviderService;

    @MockBean
    private ApiClientService apiClientService;

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
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_1);

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
                        FILING_HISTORY_TYPE_CH01));

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

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
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(FILING_HISTORY_ID)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_type",
                        is(FILING_HISTORY_TYPE_CH01)))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY_1)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST)));

        final CertifiedCopyItem retrievedCopy = assertItemSavedCorrectly(CERTIFIED_COPY_ID);
    }

    @Test
    @DisplayName("Successfully creates certified copy item with multiple filing history documents")
    void createCertifiedCopyItemMultipleFilingHistorySuccessfullyCreatesCertifiedCopyItem() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
                = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_1);

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

        final List<FilingHistoryDocument> filings = getFilingHistoryDocumentsMulti();
        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

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
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(FILING_HISTORY_ID_01)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_type",
                        is(FILING_HISTORY_TYPE_CH01)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_id",
                        is(FILING_HISTORY_ID_02)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_type",
                        is(FILING_HISTORY_TYPE_NEWINC)))
                .andExpect(jsonPath("$.item_costs[0].discount_applied",
                        is(DISCOUNT)))
                .andExpect(jsonPath("$.item_costs[0].item_cost",
                        is(Integer.toString(CERTIFIED_COPY_COST))))
                .andExpect(jsonPath("$.item_costs[0].calculated_cost",
                        is(Integer.toString(CERTIFIED_COPY_COST))))
                .andExpect(jsonPath("$.item_costs[0].product_type",
                        is(CERTIFIED_COPY.getJsonName())))
                .andExpect(jsonPath("$.item_costs[1].discount_applied",
                        is(DISCOUNT)))
                .andExpect(jsonPath("$.item_costs[1].item_cost",
                        is(Integer.toString(CERTIFIED_COPY_NEW_INCORPORATION_COST))))
                .andExpect(jsonPath("$.item_costs[1].calculated_cost",
                        is(Integer.toString(CERTIFIED_COPY_NEW_INCORPORATION_COST))))
                .andExpect(jsonPath("$.item_costs[1].product_type",
                        is(CERTIFIED_COPY_INCORPORATION.getJsonName())))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY_1)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST_MULTI)));

        final CertifiedCopyItem retrievedCopy = assertItemSavedCorrectly(CERTIFIED_COPY_ID);
        assertThat(retrievedCopy.getData().getItemCosts().size(), is(2));
        final ItemCosts cost1 = retrievedCopy.getData().getItemCosts().get(0);
        final ItemCosts cost2 = retrievedCopy.getData().getItemCosts().get(1);
        assertThat(cost1.getDiscountApplied(), is(DISCOUNT));
        assertThat(cost1.getItemCost(), is(Integer.toString(CERTIFIED_COPY_COST)));
        assertThat(cost1.getCalculatedCost(), is(Integer.toString(CERTIFIED_COPY_COST)));
        assertThat(cost1.getProductType(), is(CERTIFIED_COPY));
        assertThat(cost2.getDiscountApplied(), is(DISCOUNT));
        assertThat(cost2.getItemCost(), is(Integer.toString(CERTIFIED_COPY_NEW_INCORPORATION_COST)));
        assertThat(cost2.getCalculatedCost(), is(Integer.toString(CERTIFIED_COPY_NEW_INCORPORATION_COST)));
        assertThat(cost2.getProductType(), is(CERTIFIED_COPY_INCORPORATION));
    }

    @Test
    @DisplayName("Successfully creates certified copy item with multiple filing history documents and more than 1 quantity")
    void createCertifiedCopyItemMultipleFilingHistoryAndQuantityOver1SuccessfullyCreatesCertifiedCopyItem() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
                = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_3);

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

        final List<FilingHistoryDocument> filings = getFilingHistoryDocumentsMulti();
        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

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
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(FILING_HISTORY_ID_01)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_type",
                        is(FILING_HISTORY_TYPE_CH01)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_date",
                        is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_description",
                        is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_description_values",
                        is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_id",
                        is(FILING_HISTORY_ID_02)))
                .andExpect(jsonPath("$.item_options.filing_history_documents[1].filing_history_type",
                        is(FILING_HISTORY_TYPE_NEWINC)))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY_3)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST_MULTI_QUANTITY_3)));

        final CertifiedCopyItem retrievedCopy = assertItemSavedCorrectly(CERTIFIED_COPY_ID);
    }

    @Test
    @DisplayName("Successfully creates certified copy with delivery as collection and same-day")
    void createCertifiedCopySetsDeliveryMethodCollectionAndDeliveryTimeScaleSameDay() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_1);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setDeliveryTimescale(DeliveryTimescale.SAME_DAY);
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.COLLECTION);
        certifiedCopyItemOptionsDTORequest.setForename(FORENAME);
        certifiedCopyItemOptionsDTORequest.setSurname(SURNAME);
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);
        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

        final List<FilingHistoryDocument> filings =
                singletonList(new FilingHistoryDocument(FILING_HISTORY_DATE,
                        FILING_HISTORY_DESCRIPTION,
                        FILING_HISTORY_DESCRIPTION_VALUES,
                        FILING_HISTORY_ID,
                        FILING_HISTORY_TYPE_CH01));
        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.item_costs[0].product_type",
                        is(ProductType.CERTIFIED_COPY_SAME_DAY.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_method",
                        is(DeliveryMethod.COLLECTION.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_timescale",
                        is(DeliveryTimescale.SAME_DAY.getJsonName())))
                .andExpect(jsonPath("$.postal_delivery", is(false)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY_1)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST_SAME_DAY)));

        assertItemSavedCorrectly(CERTIFIED_COPY_ID);

    }

    @Test
    @DisplayName("Successfully creates certified copy with default delivery method and timescale")
    void createCertifiedCopyDefaultsDeliveryMethodAndDeliveryTimeScale() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = getCertifiedCopyItemRequestDTO();

        List<FilingHistoryDocument> filingHistoryDocuments = getFilingHistoryDocuments();

        when(filingHistoryDocumentService.getFilingHistoryDocuments(anyString(), anyList())).thenReturn(filingHistoryDocuments);
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
                .andExpect(jsonPath("$.quantity", is(QUANTITY_1)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST_NEWINC)));

        assertItemSavedCorrectly(CERTIFIED_COPY_ID);

    }

    @Test
    @DisplayName("Successfully creates certified copy with correct descriptions")
    void createCertifiedCopyPopulatesDescription() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = getCertifiedCopyItemRequestDTO();

        List<FilingHistoryDocument> filingHistoryDocuments = getFilingHistoryDocuments();

        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(descriptionProviderService.getDescription(COMPANY_NUMBER)).thenReturn(DESCRIPTION);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(anyString(), anyList())).thenReturn(filingHistoryDocuments);

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
                        is(DESCRIPTION)))
                .andExpect(jsonPath("$.total_item_cost", is(TOTAL_ITEM_COST_NEWINC)));


        assertItemSavedCorrectly(CERTIFIED_COPY_ID);

    }

    private List<FilingHistoryDocument> getFilingHistoryDocuments() {
        List<FilingHistoryDocument> filingHistoryDocuments = new LinkedList<>();
        FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
        filingHistoryDocument.setFilingHistoryType(FILING_HISTORY_TYPE_NEWINC);
        filingHistoryDocuments.add(filingHistoryDocument);
        return filingHistoryDocuments;
    }

    private CertifiedCopyItemRequestDTO getCertifiedCopyItemRequestDTO() {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_1);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);
        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);
        return certifiedCopyItemDTORequest;
    }

    @Test
    @DisplayName("Fails to create certified copy when forename/surname missing with delivery set as collection")
    void createCertifiedCopyWithDeliveryMethodCollectionAndMissingForenameSurnameFails() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY_1);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.COLLECTION);
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);
        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

        final List<FilingHistoryDocument> filings =
                singletonList(new FilingHistoryDocument(FILING_HISTORY_DATE,
                        FILING_HISTORY_DESCRIPTION,
                        FILING_HISTORY_DESCRIPTION_VALUES,
                        FILING_HISTORY_ID,
                        FILING_HISTORY_TYPE_CH01));
        when(idGeneratorService.autoGenerateId()).thenReturn(CERTIFIED_COPY_ID);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocuments(eq(COMPANY_NUMBER), anyList())).thenReturn(filings);

        mockMvc.perform(post(CERTIFIED_COPIES_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certifiedCopyItemDTORequest)))
                .andExpect(status().isBadRequest());
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
        final CertifiedCopyItemData certifiedCopyItemData = new CertifiedCopyItemData();
        certifiedCopyItemData.setCompanyName(COMPANY_NAME);
        certifiedCopyItemData.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemData.setId(CERTIFIED_COPY_ID);
        certifiedCopyItemData.setQuantity(QUANTITY_1);
        certifiedCopyItemData.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemData.setEtag(TOKEN_ETAG);
        certifiedCopyItemData.setLinks(LINKS);
        certifiedCopyItemData.setPostageCost(POSTAGE_COST);
        final CertifiedCopyItem newItem = new CertifiedCopyItem();
        newItem.setCompanyNumber(COMPANY_NUMBER);
        newItem.setId(CERTIFIED_COPY_ID);
        newItem.setQuantity(QUANTITY_1);
        newItem.setUserId(ERIC_IDENTITY_VALUE);
        newItem.setData(certifiedCopyItemData);
        newItem.setCustomerReference(CUSTOMER_REFERENCE);
        newItem.setEtag(TOKEN_ETAG);
        newItem.setLinks(LINKS);
        newItem.setPostageCost(POSTAGE_COST);
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(new FilingHistoryDocument(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION, FILING_HISTORY_DESCRIPTION_VALUES, FILING_HISTORY_ID, FILING_HISTORY_TYPE_CH01)));
        newItem.setItemOptions(options);
        repository.save(newItem);

        final CertifiedCopyItemResponseDTO expectedItem = new CertifiedCopyItemResponseDTO();
        expectedItem.setCompanyNumber(COMPANY_NUMBER);
        expectedItem.setCompanyName(COMPANY_NAME);
        expectedItem.setQuantity(QUANTITY_1);
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
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
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
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    private List<FilingHistoryDocument> getFilingHistoryDocumentsMulti() {
        FilingHistoryDocument filingHistoryDocument1 = new FilingHistoryDocument();
        filingHistoryDocument1.setFilingHistoryId(FILING_HISTORY_ID_01);
        filingHistoryDocument1.setFilingHistoryType(FILING_HISTORY_TYPE_CH01);
        filingHistoryDocument1.setFilingHistoryDate(FILING_HISTORY_DATE);
        filingHistoryDocument1.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION);
        filingHistoryDocument1.setFilingHistoryDate(FILING_HISTORY_DATE);
        filingHistoryDocument1.setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_VALUES);
        FilingHistoryDocument filingHistoryDocument2 = new FilingHistoryDocument();
        filingHistoryDocument2.setFilingHistoryId(FILING_HISTORY_ID_02);
        filingHistoryDocument2.setFilingHistoryType(FILING_HISTORY_TYPE_NEWINC);
        filingHistoryDocument2.setFilingHistoryDate(FILING_HISTORY_DATE);
        filingHistoryDocument2.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION);
        filingHistoryDocument2.setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_VALUES);
        List<FilingHistoryDocument> filingHistoryDocumentList = new ArrayList<>();
        filingHistoryDocumentList.add(filingHistoryDocument1);
        filingHistoryDocumentList.add(filingHistoryDocument2);

        return filingHistoryDocumentList;
    }

    /**
     * Verifies that the certified copy item can be retrieved
     * from the database using its expected ID value.
     * @param certifiedCopyId the expected ID of the newly created item
     * @return the retrieved certified copy item, for possible further verification
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
        assertThat(retrievedFiling.getFilingHistoryType(), is(FILING_HISTORY_TYPE_CH01));
    }

}
