package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_OAUTH2_VALUE;

@AutoConfigureMockMvc
@SpringBootTest
public class CertifiedCopiedControllerIntegrationTest {

    private static final String CERTIFIED_COPIED_URL = "/orderable/certified-copies";

    private static final String COMPANY_NUMBER = "00000000";
    private static final String CUSTOMER_REFERENCE = "Certificate ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Successfully creates certified copy item")
    void createCertifiedCopyItemSuccessfullyCreatesCertifiedCopyItem() throws Exception {
        final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest = new CertifiedCopyItemRequestDTO();
        certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
        certifiedCopyItemDTORequest.setQuantity(QUANTITY);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setContactNumber(CONTACT_NUMBER);
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.POSTAL);
        certifiedCopyItemOptionsDTORequest.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        certifiedCopyItemOptionsDTORequest.setForename(FORENAME);
        certifiedCopyItemOptionsDTORequest.setSurname(SURNAME);

        certifiedCopyItemOptionsDTORequest.setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

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
                .andExpect(jsonPath("$.item_options.delivery_method", is(DeliveryMethod.POSTAL.getJsonName())))
                .andExpect(jsonPath("$.item_options.delivery_timescale", is(DeliveryTimescale.STANDARD.getJsonName())))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id", is(FILING_HISTORY_ID)))
                .andExpect(jsonPath("$.item_options.forename", is(FORENAME)))
                .andExpect(jsonPath("$.item_options.surname", is(SURNAME)))
                .andExpect(jsonPath("$.postal_delivery", is(true)))
                .andExpect(jsonPath("$.quantity", is(QUANTITY)));
    }
}
