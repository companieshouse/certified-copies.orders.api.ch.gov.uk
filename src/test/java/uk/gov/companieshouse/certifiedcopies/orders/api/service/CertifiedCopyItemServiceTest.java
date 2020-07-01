package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * Unit tests the {@link CertifiedCopyItemService} class.
 */
@ExtendWith(MockitoExtension.class)
public class CertifiedCopyItemServiceTest {

    private static final String ID = "CCD-123456-123456";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String DESCRIPTION = "certified copy for company 00000000";
    private static final String DESCRIPTION_IDENTIFIER = "certified-copy";
    private static final String COMPANY_NUMBER_KEY = "company_number";

    @InjectMocks
    private CertifiedCopyItemService serviceUnderTest;

    @Mock
    private EtagGeneratorService etagGenerator;

    @Mock
    private LinksGeneratorService linksGenerator;

    @Mock
    private IdGeneratorService idGeneratorService;

    @Mock
    private DescriptionProviderService descriptionProviderService;

    @Mock
    private CertifiedCopyItemRepository repository;

    @Test
    @DisplayName("createCertifiedCopyItem creates and saves the certified copy item with id, timestamps, etag and links")
    void createCertifiedCopyItemPopulatesAndSavesItem() {
        when(idGeneratorService.autoGenerateId()).thenReturn(ID);

        final CertifiedCopyItemOptions certifiedCopyItemOptions = new CertifiedCopyItemOptions();
        certifiedCopyItemOptions.setDeliveryMethod(DeliveryMethod.POSTAL);
        final CertifiedCopyItem certifiedCopyItem = new CertifiedCopyItem();
        certifiedCopyItem.setItemOptions(certifiedCopyItemOptions);

        when(repository.save(certifiedCopyItem)).thenReturn(certifiedCopyItem);

        final LocalDateTime intervalStart = LocalDateTime.now();

        serviceUnderTest.createCertifiedCopyItem(certifiedCopyItem);

        final LocalDateTime intervalEnd = LocalDateTime.now();

        verifyCreationTimestampsWithinExecutionInterval(certifiedCopyItem, intervalStart, intervalEnd);
        assertThat(certifiedCopyItem.getId(), is(ID));
        verify(etagGenerator).generateEtag();
        verify(linksGenerator).generateLinks(ID);
    }

    @Test
    @DisplayName("createCertifiedCopyItem creates and saves the certified copy item with the descriptions")
    void createCertifiedCopyItemPopulatesDescriptionAndSavesItem() {
        when(idGeneratorService.autoGenerateId()).thenReturn(ID);
        when(descriptionProviderService.getDescription(COMPANY_NUMBER)).thenReturn(DESCRIPTION);

        final CertifiedCopyItemOptions certifiedCopyItemOptions = new CertifiedCopyItemOptions();
        certifiedCopyItemOptions.setDeliveryMethod(DeliveryMethod.POSTAL);
        final CertifiedCopyItem certifiedCopyItem = new CertifiedCopyItem();
        certifiedCopyItem.setCompanyNumber(COMPANY_NUMBER);
        certifiedCopyItem.setItemOptions(certifiedCopyItemOptions);

        when(repository.save(certifiedCopyItem)).thenReturn(certifiedCopyItem);

        final LocalDateTime intervalStart = LocalDateTime.now();

        serviceUnderTest.createCertifiedCopyItem(certifiedCopyItem);

        final LocalDateTime intervalEnd = LocalDateTime.now();

        verifyCreationTimestampsWithinExecutionInterval(certifiedCopyItem, intervalStart, intervalEnd);
        assertThat(certifiedCopyItem.getData().getDescription(), is(DESCRIPTION));
        assertThat(certifiedCopyItem.getData().getDescriptionIdentifier(), is(DESCRIPTION_IDENTIFIER));
        assertThat(certifiedCopyItem.getData().getDescriptionValues().get(DESCRIPTION_IDENTIFIER), is(DESCRIPTION));
        assertThat(certifiedCopyItem.getData().getDescriptionValues().get(COMPANY_NUMBER_KEY), is(COMPANY_NUMBER));
        verify(descriptionProviderService).getDescription(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("getCertifiedCopyItemById retrieves undecorated item")
    void getCertifiedCopyItemByIdRetrievesItem() {

        // Given
        final CertifiedCopyItem item = new CertifiedCopyItem();
        when(repository.findById(ID)).thenReturn(Optional.of(item));

        // When
        final Optional<CertifiedCopyItem> itemRetrieved = serviceUnderTest.getCertifiedCopyItemById(ID);

        // Then
        verify(repository).findById(ID);
        assertThat(itemRetrieved.isPresent(), is(true));
        verify(etagGenerator, never()).generateEtag();
    }

    @Test
    @DisplayName("getCertifiedCopyItemById handles failure to find item smoothly")
    void getCertifiedCopyItemByIdHandlesFailureToFindItemSmoothly() {

        // Given
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // When
        final Optional<CertifiedCopyItem> item = serviceUnderTest.getCertifiedCopyItemById(ID);
        assertThat(item.isPresent(), is(false));
    }


    /**
     * Verifies that the item created at and updated at timestamps are within the expected interval
     * for item creation.
     * @param itemCreated the item created
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    private void verifyCreationTimestampsWithinExecutionInterval(final CertifiedCopyItem itemCreated,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {
        assertThat(itemCreated.getCreatedAt().isAfter(intervalStart) ||
                itemCreated.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getCreatedAt().isBefore(intervalEnd) ||
                itemCreated.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(itemCreated.getUpdatedAt().isAfter(intervalStart) ||
                itemCreated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getUpdatedAt().isBefore(intervalEnd) ||
                itemCreated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }
}
