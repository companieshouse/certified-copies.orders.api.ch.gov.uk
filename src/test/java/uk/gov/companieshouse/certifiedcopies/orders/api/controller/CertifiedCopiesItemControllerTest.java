package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.certifiedcopies.orders.api.validator.CreateCertifiedCopyItemRequestValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;

import javax.servlet.http.HttpServletRequest;

/**
 * Unit tests the {@link CertifiedCopiesItemController} class.
 */
@ExtendWith(MockitoExtension.class)
public class CertifiedCopiesItemControllerTest {

    private static final String ID = "CCD-123456-123456";

    @InjectMocks
    private CertifiedCopiesItemController controllerUnderTest;

    @Mock
    private CertifiedCopyItemService mockCertifiedCopyItemService;

    @Mock
    private CertifiedCopyItem item;

    @Mock
    private CertifiedCopyItemData data;

    @Mock
    private CertifiedCopyItemResponseDTO dto;

    @Mock
    private CertifiedCopyItemMapper mapper;

    @Mock
    private CreateCertifiedCopyItemRequestValidator validator;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private FilingHistoryDocumentService mockFilingHistoryDocumentService;

    private static final String COMPANY_NUMBER = "00000000";
    private static final String REQUEST_ID = "abcdefg12345678";
    private static final String COMPANY_NAME = "test company";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String FILING_HISTORY_TYPE  = "NEWINC";

    @Test
    @DisplayName("POST certified copy successful")
    void postCertifiedCopySuccessful() {

        List<String> emptyErrors = new ArrayList<>();

        List<FilingHistoryDocument> filingHistoryDocuments = new ArrayList<>();
        FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
        filingHistoryDocument.setFilingHistoryId(FILING_HISTORY_ID);
        filingHistoryDocument.setFilingHistoryType(FILING_HISTORY_TYPE);
        filingHistoryDocuments.add(filingHistoryDocument);

        CertifiedCopyItemOptions itemOptions = new CertifiedCopyItemOptions();
        itemOptions.setFilingHistoryDocuments(filingHistoryDocuments);

        CertifiedCopyItemData data = new CertifiedCopyItemData();
        data.setCompanyNumber(COMPANY_NUMBER);
        data.setItemOptions(itemOptions);

        CertifiedCopyItem item = new CertifiedCopyItem();
        item.setData(data);

        when(validator.getValidationErrors(any())).thenReturn(emptyErrors);

        when(mapper.certifiedCopyItemRequestDTOToCertifiedCopyItem(any())).thenReturn(item);

        when(mockCompanyService.getCompanyName(anyString())).thenReturn(COMPANY_NAME);

        when(mockFilingHistoryDocumentService.getFilingHistoryDocuments(item.getData().getCompanyNumber()
            , item.getData().getItemOptions().getFilingHistoryDocuments())).thenReturn(filingHistoryDocuments);

        when(mockCertifiedCopyItemService.createCertifiedCopyItem(item)).thenReturn(item);

        when(mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(item.getData())).thenReturn(new CertifiedCopyItemResponseDTO());

        ResponseEntity<Object> response =
            controllerUnderTest.createCertifiedCopy
                (buildCreateCertifiedCopyItemRequest(COMPANY_NUMBER), mockHttpServletRequest, REQUEST_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    @DisplayName("POST certified copy returns bad request when errors returned")
    void postCertifiedCopyReturnsBadRequest() {

        List<String> errors = new ArrayList<>();
        errors.add("error");

        when(validator.getValidationErrors(any())).thenReturn(errors);

        ResponseEntity<Object> response =
            controllerUnderTest.createCertifiedCopy
                (buildCreateCertifiedCopyItemRequest(COMPANY_NUMBER), mockHttpServletRequest, REQUEST_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Get certified copy item resource returned")
    void getCertifiedCopyItemIsPresent() {
        when(mockCertifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.of(item));
        when(item.getData()).thenReturn(data);
        when(mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(data)).thenReturn(dto);
        ResponseEntity<Object> response = controllerUnderTest.getCertifiedCopy(ID, REQUEST_ID_VALUE);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    @DisplayName("Get certified copy item resource returns HTTP NOT FOUND")
    void getCertifiedCopyItemNotFound() {
        when(mockCertifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.empty());
        ResponseEntity<Object> response = controllerUnderTest.getCertifiedCopy(ID, REQUEST_ID_VALUE);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    private CertifiedCopyItemRequestDTO buildCreateCertifiedCopyItemRequest(final String companyNumber) {
        final CertifiedCopyItemRequestDTO request = new CertifiedCopyItemRequestDTO();
        request.setCompanyNumber(companyNumber);
        request.setCustomerReference(CUSTOMER_REFERENCE);
        request.setQuantity(QUANTITY);

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
            .setFilingHistoryDocuments(singletonList(filingHistoryDocumentRequestDTO));
        request.setItemOptions(certifiedCopyItemOptionsDTORequest);
        return request;
    }
}
