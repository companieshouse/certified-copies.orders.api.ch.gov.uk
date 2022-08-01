package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.json.JsonMergePatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.PatchMerger;
import uk.gov.companieshouse.certifiedcopies.orders.api.validator.PatchItemRequestValidator;

/**
 * Unit tests the {@link CertifiedCopiesItemController} class.
 */
@ExtendWith(MockitoExtension.class)
public class CertifiedCopiesItemControllerTest {

    private static final String ID = "CCD-123456-123456";

    @InjectMocks
    private CertifiedCopiesItemController controllerUnderTest;

    @Mock
    private CertifiedCopyItemService certifiedCopyItemService;

    @Mock
    private CertifiedCopyItem item;

    @Mock
    private CertifiedCopyItemData data;

    @Mock
    private CertifiedCopyItemResponseDTO dto;

    @Mock
    private CertifiedCopyItemMapper mapper;

    @Mock
    private PatchMerger merger;

    @Mock
    private JsonMergePatch patch;

    @Mock
    private CertifiedCopyItemOptions certifiedCopyItemOptions;

    @Mock
    private PatchItemRequestValidator validator;

    @Test
    @DisplayName("Get certified copy item resource returned")
    void getCertifiedCopyItemIsPresent() {
        when(certifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.of(item));
        when(item.getData()).thenReturn(data);
        when(mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(data)).thenReturn(dto);
        ResponseEntity<Object> response = controllerUnderTest.getCertifiedCopy(ID, REQUEST_ID_VALUE);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    @DisplayName("Get certified copy item resource returns HTTP NOT FOUND")
    void getCertifiedCopyItemNotFound() {
        when(certifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.empty());
        ResponseEntity<Object> response = controllerUnderTest.getCertifiedCopy(ID, REQUEST_ID_VALUE);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Update request updates successfully")
    void updateUpdatesSuccessfully() {
        // Given
        when(certifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.of(item));
        when(merger.mergePatch(patch, item, CertifiedCopyItem.class)).thenReturn(item);
        when(item.getCompanyNumber()).thenReturn("12345678");
        when(certifiedCopyItemService.saveCertifiedCopyItem(item)).thenReturn(item);
        when(mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(item.getData())).thenReturn(dto);

        // When
        final ResponseEntity<Object> response = controllerUnderTest.updateCertifiedCopyItem(patch, ID,
                REQUEST_ID_VALUE);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    @DisplayName("Update request reports resource not found")
    void updateReportsResourceNotFound() {
        when(certifiedCopyItemService.getCertifiedCopyItemById(ID)).thenReturn(Optional.empty());
        final ResponseEntity<Object> response = controllerUnderTest.updateCertifiedCopyItem(patch, ID,
                REQUEST_ID_VALUE);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));

    }

//    @Test
//    @DisplayName("Update certificate item supplied patch has validation errors")
//    void updateCertificateItemPatchValidationErrors() {
//
//        when(validator.getValidationErrors(patch))
//                .thenReturn(Collections.singletonList(ApiErrors.ERR_SURNAME_REQUIRED));
//
//        ResponseEntity<Object> response = controllerUnderTest.updateCertificateItem(patch, ID,
//                REQUEST_ID_VALUE);
//        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
//    }

    @Test
    @DisplayName("Update certificate item patched certificate has validation errors")
    void updateCertificateItemMergedValidationErrors() {
        List<ApiError> errors = Collections.singletonList(ApiErrors.ERR_JSON_PROCESSING);
        when(validator.getValidationErrors(patch)).thenReturn(errors);

        ResponseEntity<Object> response = controllerUnderTest.updateCertifiedCopyItem(patch, ID,
                REQUEST_ID_VALUE);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

}
