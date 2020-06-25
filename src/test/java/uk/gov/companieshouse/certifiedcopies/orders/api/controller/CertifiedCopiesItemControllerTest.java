package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.REQUEST_ID_VALUE;

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


}
