package uk.gov.companieshouse.certifiedcopies.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CollectionLocation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.Links;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(CertifiedCopyItemMapperTest.Config.class)
public class CertifiedCopyItemMapperTest {

    private static final String COMPANY_NUMBER = "00000000";
    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final Integer QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";

    private static final String ID = "CCD-123456-123456";
    private static final String COMPANY_NAME = "Company Name";
    private static final String DESCRIPTION = "Description";
    private static final String DESCRIPTION_IDENTIFIER = "Description";
    private static final Map<String, String> DESCRIPTION_VALUES = singletonMap("key1", "value1");
    private static final String ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final List<ItemCosts> ITEM_COSTS;
    private static final CertifiedCopyItemOptions ITEM_OPTIONS;
    private static final String KIND = "item#certified-copy";
    private static final Links LINKS;
    private static final String POSTAGE_COST = "5.00";
    private static final Boolean IS_POSTAL_DELIVERY = Boolean.TRUE;
    private static final String TOTAL_ITEM_COST = "15.00";
    private static final List<FilingHistoryDocument> FILING_HISTORY_DOCUMENTS;

    static {
        ITEM_COSTS = new ArrayList<>();
        ITEM_COSTS.add(new ItemCosts());

        FILING_HISTORY_DOCUMENTS = new ArrayList<>();
        FILING_HISTORY_DOCUMENTS.add(new FilingHistoryDocument());

        ITEM_OPTIONS = new CertifiedCopyItemOptions();
        ITEM_OPTIONS.setCollectionLocation(CollectionLocation.CARDIFF);
        ITEM_OPTIONS.setContactNumber(CONTACT_NUMBER);
        ITEM_OPTIONS.setDeliveryMethod(DeliveryMethod.POSTAL);
        ITEM_OPTIONS.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        ITEM_OPTIONS.setFilingHistoryDocuments(FILING_HISTORY_DOCUMENTS);
        ITEM_OPTIONS.setForename(FORENAME);
        ITEM_OPTIONS.setSurname(SURNAME);

        LINKS = new Links();
        LINKS.setSelf("self");
    }

    @Configuration
    @ComponentScan(basePackageClasses = CertifiedCopyItemMapperTest.class)
    static class Config {}

    @Autowired
    private CertifiedCopyItemMapper mapperUnderTest;

    @Test
    public void testCertifiedCopyItemRequestDTOToCertifiedCopyItem() {
        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsRequestDTO
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsRequestDTO
                .setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemOptionsRequestDTO.setContactNumber(CONTACT_NUMBER);
        certifiedCopyItemOptionsRequestDTO.setDeliveryMethod(DeliveryMethod.POSTAL);
        certifiedCopyItemOptionsRequestDTO
                .setDeliveryTimescale(DeliveryTimescale.STANDARD);
        certifiedCopyItemOptionsRequestDTO
                .setCollectionLocation(CollectionLocation.CARDIFF);
        certifiedCopyItemOptionsRequestDTO.setForename(FORENAME);
        certifiedCopyItemOptionsRequestDTO.setSurname(SURNAME);

        final CertifiedCopyItemRequestDTO dto = new CertifiedCopyItemRequestDTO();
        dto.setCompanyNumber(COMPANY_NUMBER);
        dto.setCustomerReference(CUSTOMER_REFERENCE);
        dto.setQuantity(QUANTITY);
        dto.setItemOptions(certifiedCopyItemOptionsRequestDTO);

        final CertifiedCopyItem certifiedCopyItem = mapperUnderTest
                .certifiedCopyItemRequestDTOToCertifiedCopyItem(dto);

        CertifiedCopyItemData item = certifiedCopyItem.getData();

        assertThat(item, is(notNullValue()));
        assertThat(item.getCompanyNumber(), is(dto.getCompanyNumber()));
        assertThat(item.getCustomerReference(), is(dto.getCustomerReference()));
        assertThat(item.getQuantity(), is(dto.getQuantity()));
        assertThat(item.getItemOptions().getContactNumber(),
                is(dto.getItemOptions().getContactNumber()));
        assertThat(item.getItemOptions().getDeliveryMethod(),
                is(dto.getItemOptions().getDeliveryMethod()));
        assertThat(item.getItemOptions().getDeliveryTimescale(),
                is(dto.getItemOptions().getDeliveryTimescale()));
        assertThat(item.getItemOptions().getCollectionLocation(),
                is(dto.getItemOptions().getCollectionLocation()));
        assertThat(item.getItemOptions().getForename(), is(dto.getItemOptions().getForename()));
        assertThat(item.getItemOptions().getSurname(), is(dto.getItemOptions().getSurname()));
        assertThat(item.getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryId(),
                is(dto.getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryId()));
    }

    @Test
    public void testCertifiedCopyItemRequestDTOToCertifiedCopyItemDefaults() {
        final CertifiedCopyItemRequestDTO dto = new CertifiedCopyItemRequestDTO();
        dto.setItemOptions(new CertifiedCopyItemOptionsRequestDTO());

        final CertifiedCopyItem certifiedCopyItem = mapperUnderTest
                .certifiedCopyItemRequestDTOToCertifiedCopyItem(dto);

        CertifiedCopyItemData item = certifiedCopyItem.getData();
        assertThat(item.getItemOptions().getDeliveryMethod(), is(DeliveryMethod.POSTAL));
    }

    @Test
    public void testCertifiedCopyItemDataToCertifiedCopyItemResponseDTO() {
        final CertifiedCopyItemData item = new CertifiedCopyItemData();
        item.setId(ID);
        item.setCompanyName(COMPANY_NAME);
        item.setCompanyNumber(COMPANY_NUMBER);
        item.setCustomerReference(CUSTOMER_REFERENCE);
        item.setDescription(DESCRIPTION);
        item.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        item.setDescriptionValues(DESCRIPTION_VALUES);
        item.setEtag(ETAG);
        item.setItemCosts(ITEM_COSTS);
        item.setItemOptions(ITEM_OPTIONS);
        item.setKind(KIND);
        item.setLinks(LINKS);
        item.setPostageCost(POSTAGE_COST);
        item.setPostalDelivery(IS_POSTAL_DELIVERY);
        item.setQuantity(QUANTITY);
        item.setTotalItemCost(TOTAL_ITEM_COST);

        final CertifiedCopyItemResponseDTO dto = mapperUnderTest
                .certifiedCopyItemDataToCertifiedCopyItemResponseDTO(item);

        assertThat(dto.getId(), is(item.getId()));
        assertThat(dto.getCompanyName(), is(item.getCompanyName()));
        assertThat(dto.getCompanyNumber(), is(item.getCompanyNumber()));
        assertThat(dto.getCustomerReference(), is(item.getCustomerReference()));
        assertThat(dto.getDescription(), is(item.getDescription()));
        assertThat(dto.getDescriptionIdentifier(), is(item.getDescriptionIdentifier()));
        assertThat(dto.getDescriptionValues(), is(item.getDescriptionValues()));
        assertThat(dto.getEtag(), is(item.getEtag()));
        assertThat(dto.getItemCosts(), is(item.getItemCosts()));

        assertThat(dto.getItemOptions().getCollectionLocation(),
                is(item.getItemOptions().getCollectionLocation()));
        assertThat(dto.getItemOptions().getContactNumber(),
                is(item.getItemOptions().getContactNumber()));
        assertThat(dto.getItemOptions().getDeliveryMethod(),
                is(item.getItemOptions().getDeliveryMethod()));
        assertThat(dto.getItemOptions().getDeliveryTimescale(),
                is(item.getItemOptions().getDeliveryTimescale()));
        assertThat(dto.getItemOptions().getFilingHistoryDocuments().get(0),
                is(item.getItemOptions().getFilingHistoryDocuments().get(0)));
        assertThat(dto.getItemOptions().getForename(), is(item.getItemOptions().getForename()));
        assertThat(dto.getItemOptions().getSurname(), is(item.getItemOptions().getSurname()));

        assertThat(dto.getKind(), is(item.getKind()));
        assertThat(dto.getLinks().getSelf(), is(item.getLinks().getSelf()));
        assertThat(dto.getPostageCost(), is(item.getPostageCost()));
        assertThat(dto.getPostalDelivery(), is(item.getPostalDelivery()));
        assertThat(dto.getQuantity(), is(item.getQuantity()));
        assertThat(dto.getTotalItemCost(), is(item.getTotalItemCost()));

    }
}
